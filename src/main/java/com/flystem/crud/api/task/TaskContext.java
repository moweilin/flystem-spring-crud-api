package com.flystem.crud.api.task;

import com.flystem.crud.api.io.ExportFile;

/**
 * 任务执行期的汇报口：进度、说明、处理条数、结果文件。由 {@link CrudTaskManager} 提供给
 * {@link TaskBody}，写入的内容经 {@link CrudTask} 快照对查询端点可见。
 *
 * <p><b>进度是粗粒度的</b>：管理器不知道业务内部走了多少，只能由任务体主动汇报。生成产物按阶段打点
 * （导出：取数 → 写文件；导入：落库 → 完成），故看到的是几个里程碑而非逐行百分比。要更细的粒度，
 * 得让 Service 层接受回调——那会改动 Service 契约，MVP 刻意不做（协议 §8）。
 *
 * <p>实现须<b>线程安全</b>（汇报发生在工作线程，快照读取发生在请求线程）。
 */
public interface TaskContext {

    /**
     * 汇报进度。
     *
     * @param percent 0–100；越界值会被夹到该区间
     * @param message 阶段说明（可空），失败时不必用本方法——抛异常即可
     */
    void progress(int percent, String message);

    /** 汇报已处理 / 将处理的记录总数（可空表示未知）。 */
    void total(long total);

    /**
     * 交出结果文件（IMPORT / EXPORT 均可）。常见为 EXPORT 的全量导出文件，也可为 IMPORT 的
     * 结果文件（如驳回 / 校验明细）。只在任务成功时有意义；未调用则
     * {@link CrudTask#filename()} 为空、下载端点返回 404。
     */
    void resultFile(ExportFile file);
}
