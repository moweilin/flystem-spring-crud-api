package com.flystem.crud.api.task;

/**
 * 异步任务的执行体：由生成的端点在<b>提交时</b>构造（通常是一个调用 Service 的 lambda），交
 * {@link CrudTaskManager#submit} 在工作线程上跑。
 *
 * <p><b>关键约束：不要在里面碰认证上下文。</b>工作线程没有 {@code SecurityContext}
 * （它是线程局部的，默认不跨线程传播），故当前租户 / 当前用户必须在<b>提交前</b>就解析好并被 lambda
 * 捕获——生成产物的做法是先 {@code serviceFactory.getService(user)} 拿到<b>已绑定租户</b>的 Service
 * （ADR 0004 / 0017），再把它闭包进任务体。这样租户边界不依赖运行期上下文，也天然可测。
 */
@FunctionalInterface
public interface TaskBody {

    /**
     * 执行任务。经 {@code context} 汇报进度与产出结果文件；抛出任何异常即视为任务失败，
     * 异常消息会进 {@link CrudTask#message()}。
     *
     * @param context 进度与结果的汇报口
     * @throws Exception 执行失败（受检异常亦可——由管理器捕获并记为 {@link TaskStatus#FAILED}）
     */
    void run(TaskContext context) throws Exception;
}
