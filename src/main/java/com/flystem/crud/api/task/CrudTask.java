package com.flystem.crud.api.task;

/**
 * 异步导入 / 导出任务的<b>不可变快照</b>——查询与列表端点的返回体元素（协议 §4.5）。
 *
 * <p>取 record 而非「类 + getter」：它是纯数据快照、字段多且全为只读，record 一并给出构造器、
 * 访问器、{@code equals} / {@code hashCode} / {@code toString}；Jackson 2.12+ 原生支持 record 序列化，
 * 不需要 {@code ParameterNamesModule}（与 {@code PageResult} 用「类 + getter」并不矛盾：那个类要在
 * getter 里做非正数兜底，是<b>行为</b>，record 表达不了）。
 *
 * <p>时间戳是 <b>ISO-8601 字符串</b>而非 {@code Instant}：那样序列化要依赖
 * {@code jackson-datatype-jsr310} 被注册，而本库不应对使用方的 Jackson 模块配置做假设。字符串形式
 * 在任何配置下都稳定可读。
 *
 * @param id          任务 id（随机 UUID 字符串，<b>不可枚举</b>——它是跨租户可见性检查之外的第二道防线）
 * @param entity      实体名
 * @param operation   操作名（{@link CrudTaskManager#IMPORT} / {@link CrudTaskManager#EXPORT}）
 * @param status      状态
 * @param progress    进度 0–100（粗粒度里程碑，见 {@link TaskContext}）
 * @param message     阶段说明；{@link TaskStatus#FAILED} 时为失败原因
 * @param total       处理的记录条数，未知时为 {@code null}
 * @param filename    结果文件名（含扩展名）；无结果文件时为 {@code null}
 * @param contentType 结果文件 MIME 类型；无结果文件时为 {@code null}
 * @param submittedAt 提交时刻（ISO-8601）
 * @param finishedAt  结束时刻（ISO-8601）；未结束为 {@code null}
 */
public record CrudTask(
        String id,
        String entity,
        String operation,
        TaskStatus status,
        int progress,
        String message,
        Long total,
        String filename,
        String contentType,
        String submittedAt,
        String finishedAt) {

    /** 是否已成功结束（可尝试下载结果文件，若该任务产出文件的话）。 */
    public boolean succeeded() {
        return status == TaskStatus.SUCCEEDED;
    }

    /** 是否已结束（成功或失败）——用于判断还需不需要继续轮询。 */
    public boolean finished() {
        return status == TaskStatus.SUCCEEDED || status == TaskStatus.FAILED;
    }

    /** 是否有可下载的结果文件。 */
    public boolean hasResultFile() {
        return filename != null && !filename.isBlank();
    }
}
