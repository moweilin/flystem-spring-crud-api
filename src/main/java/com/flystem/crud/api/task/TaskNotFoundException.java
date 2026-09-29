package com.flystem.crud.api.task;

/**
 * 任务不可见：id 不存在、已被清理，<b>或属于别的 scope（租户）</b>——三种情况刻意不区分，
 * 区分就等于给跨租户探测开一个「这个 id 存在吗」的 oracle（见 {@link CrudTaskManager}）。
 *
 * <p>由生成的任务查询端点在 {@link CrudTaskManager#find} 返回 null 时抛出。<b>对外形态由使用方决定</b>
 * （通常经 {@code @ControllerAdvice} 映射为 404），与 {@code TenantIdNotResolvableException} 同一处理
 * 方式——库不替使用方定错误响应体的形状（ADR 0020 / 0022）。
 *
 * <p>消息里<b>只回显任务 id</b>，不含 scope：scope 通常是租户值，属敏感内容。
 */
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(String taskId) {
        super("CRUD task not found or not visible: " + taskId);
    }
}
