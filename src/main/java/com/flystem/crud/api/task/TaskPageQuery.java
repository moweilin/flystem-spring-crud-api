package com.flystem.crud.api.task;

import com.flystem.crud.api.page.PageParam;

/**
 * 异步任务分页查询条件：在 {@link TaskQuery} 之上加<b>单个</b> {@code page} 字段（{@link PageParam}），
 * 与生成的实体 {@code {P}{E}PageQuery} 同形——请求体形如
 * {@code {"operation":"EXPORT","status":"SUCCEEDED","page":{"currentPage":1,"pageSize":20}}}。
 *
 * <p>端点 {@code POST /{entity}/task/page} 的请求体；返回 {@code PageResult<CrudTask>}。
 * {@code currentPage} <b>从 1 开始</b>（与 {@code PageResult} 同基准，ADR 0019）；
 * {@code pageSize} 未指定时回落 {@link PageParam#DEFAULT_PAGE_SIZE}（任务查询无实体级
 * {@code defaultPageSize}，故直接用库级默认）。
 *
 * <p><b>可变</b>（无参构造 + setter），供 Jackson 反序列化。
 */
public class TaskPageQuery extends TaskQuery {

    private PageParam page = new PageParam();

    /**
     * 分页入参——「第几页 / 每页多少条」合成<b>一个字段</b>。永不为 {@code null}
     * （客户端显式传 {@code "page":null} 时回落一个未指定的实例）。
     */
    public PageParam getPage() {
        return page == null ? new PageParam() : page;
    }

    public void setPage(PageParam page) {
        this.page = page;
    }

    /**
     * 生效页码，<b>从 1 开始</b>；未指定时为 1。
     *
     * <p>与 {@link #pageSize()} 都<b>刻意不加 {@code get} 前缀</b>：它们是「生效值」解析器而非
     * bean 属性，加了会被 Jackson 当成额外字段。
     */
    public long currentPage() {
        return getPage().getCurrentPage();
    }

    /**
     * 生效每页条数；未指定（{@code <= 0}）时为 {@link PageParam#DEFAULT_PAGE_SIZE}。
     * <b>返回值恒 {@code > 0}</b>。
     */
    public long pageSize() {
        return getPage().resolvePageSize();
    }
}
