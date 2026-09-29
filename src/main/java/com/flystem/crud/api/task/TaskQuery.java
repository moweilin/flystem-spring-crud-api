package com.flystem.crud.api.task;

/**
 * 异步任务查询条件基类（与实体侧 {@code {P}{E}Query} 同形）：承载过滤字段，<b>不带分页</b>。
 * 分页变体见 {@link TaskPageQuery}。
 *
 * <p>字段均为可选（{@code null} = 不限）：与实体查询条件一样，条件命中的是<b>全集的子集</b>，
 * 计数 / 存在性若日后单独开端点也会复用本类而非带 {@code page} 的子类。
 *
 * <p><b>可变</b>（无参构造 + setter），供 Jackson 反序列化为请求体。
 */
public class TaskQuery {

    private String operation;

    private TaskStatus status;

    /**
     * 操作名（{@link CrudTaskManager#IMPORT} / {@link CrudTaskManager#EXPORT}）；{@code null} 或不匹配时视为不限。
     * 比较时忽略大小写空白两侧的差异（客户端常传小写）。
     */
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    /** 任务状态；{@code null} 视为不限。 */
    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * 是否命中本条件（供 {@link CrudTaskManager} 实现过滤）。{@code null} 字段跳过；
     * {@code operation} 按忽略大小写、trim 后与 {@link CrudTask#operation()} 比较。
     */
    public boolean matches(CrudTask task) {
        if (task == null) {
            return false;
        }
        if (status != null && status != task.status()) {
            return false;
        }
        if (operation != null && !operation.isBlank()) {
            String want = operation.trim();
            String got = task.operation();
            if (got == null || !want.equalsIgnoreCase(got.trim())) {
                return false;
            }
        }
        return true;
    }
}
