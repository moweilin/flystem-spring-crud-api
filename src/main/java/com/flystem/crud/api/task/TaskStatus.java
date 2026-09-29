package com.flystem.crud.api.task;

/**
 * 异步导入 / 导出任务的状态（协议 §4.5）。
 *
 * <p><b>不在 {@code enums} 子包</b>：那个包只收「配置注解属性的取值域」（见其 {@code package-info}），
 * 本枚举是运行期状态机的取值，按领域放在 {@code task} 子包。
 *
 * <p>刻意<b>没有</b> {@code CANCELLED}：MVP 不提供取消端点（长任务的取消要能中断 JDBC 语句与文件
 * 写出，语义与代价都不小），故不占一个永远取不到的值。加常量是非破坏变更。
 */
public enum TaskStatus {

    /** 已提交、尚未开始执行（在队列里）。 */
    PENDING,

    /** 正在执行。 */
    RUNNING,

    /** 执行成功。若任务体交出了结果文件，方可经下载端点取回。 */
    SUCCEEDED,

    /** 执行失败；原因见 {@link CrudTask#message()}。 */
    FAILED
}
