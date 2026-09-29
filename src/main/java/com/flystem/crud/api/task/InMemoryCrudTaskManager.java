package com.flystem.crud.api.task;

import com.flystem.crud.api.io.ExportFile;
import com.flystem.crud.api.page.PageResult;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link CrudTaskManager} 的默认实现：<b>进程内</b>线程池 + <b>内存</b>保存任务状态与结果文件。
 *
 * <p><b>适用边界要说清楚</b>——本实现是「开箱能跑」的默认件，不是生产级作业系统：
 * <ul>
 *   <li><b>不持久</b>：进程重启即丢失全部任务与结果文件；</li>
 *   <li><b>不跨实例</b>：多副本部署时，提交任务的实例与接到查询 / 下载请求的实例可能不是同一个，
 *       于是查不到任务（返回 404）。多副本必须换成共享存储的实现（数据库 / Redis / 对象存储 +
 *       持久队列），声明自己的 {@link CrudTaskManager} bean 即整体替换；</li>
 *   <li><b>结果文件全量在内存</b>：大表导出会吃堆。同理，生产环境应换落盘 / 对象存储的实现。</li>
 * </ul>
 *
 * <p>线程池是<b>自建的守护线程池</b>（默认 2 线程、有界队列 100），不复用 Boot 的
 * {@code applicationTaskExecutor}：导入导出是长任务，与请求处理共用池会互相饿死；隔离开更可预测。
 * 队列满时 {@link #submit} 抛 {@link IllegalStateException} 而<b>不</b>静默降级为同步执行——静默降级
 * 会让「异步端点」在高峰期悄悄变成阻塞调用，比明确报错更难排查。
 *
 * <p>已结束的任务保留 {@code retentionMillis}（默认 30 分钟）后由下一次 {@code submit} 顺手清理；
 * 未结束的任务不清理。
 */
public class InMemoryCrudTaskManager implements CrudTaskManager {

    /** 默认工作线程数。 */
    public static final int DEFAULT_POOL_SIZE = 2;

    /** 默认等待队列容量；满了 {@link #submit} 即抛错（不静默转同步）。 */
    public static final int DEFAULT_QUEUE_CAPACITY = 100;

    /** 已结束任务的默认保留时长（毫秒）。 */
    public static final long DEFAULT_RETENTION_MILLIS = TimeUnit.MINUTES.toMillis(30);

    private final Map<String, State> tasks = new ConcurrentHashMap<>();

    private final ThreadPoolExecutor executor;

    private final long retentionMillis;

    public InMemoryCrudTaskManager() {
        this(DEFAULT_POOL_SIZE, DEFAULT_QUEUE_CAPACITY, DEFAULT_RETENTION_MILLIS);
    }

    public InMemoryCrudTaskManager(int poolSize, int queueCapacity, long retentionMillis) {
        AtomicInteger sequence = new AtomicInteger();
        this.executor = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                runnable -> {
                    Thread thread = new Thread(runnable, "flystem-crud-task-" + sequence.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                });
        this.retentionMillis = retentionMillis;
    }

    @Override
    public CrudTask submit(String entity, String operation, String scope, TaskBody body) {
        requireScope(scope);
        purgeExpired();
        State state = new State(UUID.randomUUID().toString(), entity, operation, scope);
        tasks.put(state.id, state);
        try {
            executor.execute(() -> run(state, body));
        } catch (RejectedExecutionException e) {
            tasks.remove(state.id);
            throw new IllegalStateException("flystem CRUD task queue is full; retry later, raise the "
                    + "queue capacity, or register a CrudTaskManager bean backed by a persistent queue", e);
        }
        return state.snapshot();
    }

    @Override
    public CrudTask find(String taskId, String scope) {
        requireScope(scope);
        State state = visible(taskId, scope);
        return state == null ? null : state.snapshot();
    }

    @Override
    public PageResult<CrudTask> page(String entity, String scope, TaskPageQuery query) {
        requireScope(scope);
        purgeExpired();
        TaskPageQuery q = query == null ? new TaskPageQuery() : query;
        long page = q.currentPage();
        long size = q.pageSize();
        // ISO-8601 UTC 字符串可字典序比较；倒序 = 新提交的在前
        List<CrudTask> matched = tasks.values().stream()
                .filter(state -> Objects.equals(state.entity, entity) && Objects.equals(state.scope, scope))
                .sorted(Comparator.comparing((State state) -> state.submittedAt).reversed())
                .map(State::snapshot)
                .filter(q::matches)
                .toList();
        long total = matched.size();
        int from = (int) Math.min((page - 1) * size, total);
        int to = (int) Math.min(from + size, total);
        return PageResult.of(matched.subList(from, to), page, size, total);
    }

    @Override
    public ExportFile download(String taskId, String scope) {
        requireScope(scope);
        State state = visible(taskId, scope);
        // 只有成功结束的任务才有结果文件；其余情况一律 null（端点据此回 404）
        return state != null && state.status == TaskStatus.SUCCEEDED ? state.result : null;
    }

    /** scope 必须非 null、非 blank（ADR 0022：工厂 taskScope 永不返回 null，本入口再守一道）。 */
    private static void requireScope(String scope) {
        Objects.requireNonNull(scope, "scope");
        if (scope.isBlank()) {
            throw new IllegalArgumentException("scope must not be blank");
        }
    }

    /** scope 不匹配与任务不存在同样返回 null：区分二者等于给跨租户探测开一个 oracle。 */
    private State visible(String taskId, String scope) {
        if (taskId == null) {
            return null;
        }
        State state = tasks.get(taskId);
        return state != null && Objects.equals(state.scope, scope) ? state : null;
    }

    private void run(State state, TaskBody body) {
        state.status = TaskStatus.RUNNING;
        state.startedAtMillis = System.currentTimeMillis();
        try {
            body.run(state.context());
            state.progress = 100;
            state.status = TaskStatus.SUCCEEDED;
        } catch (Exception e) {
            state.message = describe(e);
            state.status = TaskStatus.FAILED;
        } finally {
            state.finishedAt = Instant.now().toString();
            state.finishedAtMillis = System.currentTimeMillis();
        }
    }

    /** 失败原因：异常消息为空时退到类名，保证 {@code message} 总有可读内容。 */
    private static String describe(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getName() : message;
    }

    /** 清掉结束已超过保留期的任务（顺手在每次 submit 时做，避免再起一个定时线程）。 */
    private void purgeExpired() {
        long deadline = System.currentTimeMillis() - retentionMillis;
        tasks.values().removeIf(state -> state.finishedAtMillis != 0L && state.finishedAtMillis < deadline);
    }

    /** 单个任务的可变状态：写发生在工作线程、读发生在请求线程，故字段一律 volatile。 */
    private static final class State {

        private final String id;
        private final String entity;
        private final String operation;
        private final String scope;
        private final String submittedAt = Instant.now().toString();

        private volatile TaskStatus status = TaskStatus.PENDING;
        private volatile int progress;
        private volatile String message;
        private volatile Long total;
        private volatile ExportFile result;
        private volatile String finishedAt;
        private volatile long startedAtMillis;
        private volatile long finishedAtMillis;

        private State(String id, String entity, String operation, String scope) {
            this.id = id;
            this.entity = entity;
            this.operation = operation;
            this.scope = scope;
        }

        private CrudTask snapshot() {
            ExportFile file = result;
            return new CrudTask(id, entity, operation, status, progress, message, total,
                    file == null ? null : file.filename(),
                    file == null ? null : file.contentType(),
                    submittedAt, finishedAt);
        }

        private TaskContext context() {
            return new TaskContext() {

                @Override
                public void progress(int percent, String message) {
                    State.this.progress = Math.max(0, Math.min(100, percent));
                    if (message != null) {
                        State.this.message = message;
                    }
                }

                @Override
                public void total(long total) {
                    State.this.total = total;
                }

                @Override
                public void resultFile(ExportFile file) {
                    State.this.result = file;
                }
            };
        }
    }
}
