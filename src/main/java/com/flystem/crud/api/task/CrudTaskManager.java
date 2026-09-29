package com.flystem.crud.api.task;

import com.flystem.crud.api.io.ExportFile;
import com.flystem.crud.api.page.PageResult;

/**
 * 异步导入 / 导出任务的管理 SPI（协议 §4.5）：提交、查进度、分页查询、取结果文件。生成的 IMPORT / EXPORT
 * 端点<b>不再同步干活</b>——它们提交任务并立即返回 {@link CrudTask}（含任务 id），客户端随后轮询
 * 查询端点或调分页端点、成功后调下载端点取文件。
 *
 * <p>默认实现 {@link InMemoryCrudTaskManager}（自带线程池 + 内存保存结果）由
 * {@code FlystemSpringCrudAutoConfiguration} 在使用方未声明同类型 bean 时注册；声明自己的 bean 即
 * <b>整体替换</b>（换成落盘 / 对象存储 / 数据库队列 / 分布式 worker 皆可，机制同 {@code TenantIdExtractor}）。
 *
 * <h2>可见性隔离：{@code scope}</h2>
 *
 * 提交时给出的 {@code scope} 会被记住，查询 / 分页 / 下载时必须给出<b>相同</b>的 scope 才拿得到——生成产物
 * 经工厂 {@code taskScope(user)} 给出（由 {@code @ServiceGeneration.taskScope} 决定粒度：默认
 * <b>用户级</b> = {@code getUsername()}，可改为<b>租户级</b> = 租户值字符串）。于是默认下不同用户
 * 互不可见彼此的导入 / 导出任务；租户级时同租户共享、跨租户仍隔离。
 *
 * <p><b>{@code scope} 永不为 {@code null} / blank</b>——工厂解析不出即抛错，本 SPI 的实现亦应拒绝
 * null/blank 入参（默认实现在入口校验）。scope 不匹配与任务不存在<b>同样返回 {@code null}</b>
 * （单条查询 / 下载），刻意不区分——区分就等于给跨边界探测开了一个「这个 id 存在吗」的 oracle。
 * 分页端点则只返回匹配 {@code entity} + {@code scope}（再加 {@link TaskPageQuery} 过滤）的快照（无命中为空页）。
 */
public interface CrudTaskManager {

    /**
     * 异步任务操作名：导入。写入 {@link CrudTask#operation()}，与词汇表
     * {@code CrudOperation#IMPORT} 的枚举名同源；过滤见 {@link TaskQuery#getOperation()}。
     */
    String IMPORT = "IMPORT";

    /**
     * 异步任务操作名：导出。写入 {@link CrudTask#operation()}，与词汇表
     * {@code CrudOperation#EXPORT} 的枚举名同源；过滤见 {@link TaskQuery#getOperation()}。
     */
    String EXPORT = "EXPORT";

    /**
     * 提交任务（异步）。返回的快照状态为 {@link TaskStatus#PENDING}。
     *
     * @param entity    实体名（仅用于回显与排查）
     * @param operation 操作名；生成产物传 {@link #IMPORT} / {@link #EXPORT}
     * @param scope     可见性范围；<b>非 null、非 blank</b>（由工厂 {@code taskScope(user)} 给出）
     * @param body      任务体。<b>必须在提交前就把租户绑定好</b>（捕获已绑定租户的 Service），
     *                  因为工作线程没有 {@code SecurityContext}，见 {@link TaskBody}
     * @return 提交时的任务快照（含 id）
     * @throws NullPointerException     {@code scope} 为 {@code null}
     * @throws IllegalArgumentException {@code scope} 为 blank
     * @throws IllegalStateException    队列已满、无法接收新任务（默认实现有界队列，见其 Javadoc）
     */
    CrudTask submit(String entity, String operation, String scope, TaskBody body);

    /**
     * 查任务快照。
     *
     * @param scope 可见性范围；<b>非 null、非 blank</b>
     * @return 快照；任务不存在、已被清理、<b>或 scope 不匹配</b>时一律返回 {@code null}
     */
    CrudTask find(String taskId, String scope);

    /**
     * 分页查询可见任务：仅本 {@code entity} 且相同 {@code scope}，再按 {@link TaskPageQuery}
     * 的过滤字段收窄，按 {@code submittedAt} 倒序（新→旧）分页。本 SPI 是全应用共享 bean，
     * 故必须以实体名收窄——否则 Book 控制器会列出 Account 的任务。
     *
     * @param entity 实体名（与 {@link #submit} 时写入的一致）
     * @param scope  可见性范围；<b>非 null、非 blank</b>
     * @param query  分页查询条件（过滤 + {@code page}）；{@code null} 视为空条件（仅实体 + scope，
     *               第 1 页、库级默认每页条数）
     * @return 一页快照；永不 {@code null}——无命中则为空页（列表无命中是正常态，不抛
     *         {@link TaskNotFoundException}）
     */
    PageResult<CrudTask> page(String entity, String scope, TaskPageQuery query);

    /**
     * 取结果文件。
     *
     * @param scope 可见性范围；<b>非 null、非 blank</b>
     * @return 结果文件；任务不存在 / scope 不匹配 / 尚未成功 / 未交出结果文件时返回 {@code null}
     *         （生成的下载端点据此回 404）
     */
    ExportFile download(String taskId, String scope);
}
