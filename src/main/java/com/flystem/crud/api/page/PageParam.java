package com.flystem.crud.api.page;

/**
 * 分页入参（分页的<b>请求侧</b>，与出参 {@link PageResult} 成对）：把「第几页 / 每页多少条」合成
 * 一个可嵌套的字段，供生成的查询条件类（{@code {P}{E}PageQuery}）以单个属性持有，于是请求体形如
 * {@code {"page":{"currentPage":2,"pageSize":20}}}，而不是把两个分页标量摊在条件类的顶层。
 *
 * <p><b>页码从 1 开始</b>，与 {@link PageResult#getCurrentPage()} 同基准（ADR 0019）、与 MyBatis-Plus 的
 * {@code Page} 一致；<b>不是</b> Spring Data 的 0 起。
 *
 * <p><b>{@code <= 0} 一律视为「未指定」而非一个合法取值</b>，这是本类的核心约定，两个理由：
 * <ol>
 *   <li>让<b>部分载荷</b>能正确回落：Jackson 反序列化嵌套对象时会新建本类实例，客户端只给
 *       {@code {"page":{"currentPage":2}}} 时 {@code pageSize} 就是 0。若把 0 当合法值，实体级配置的
 *       {@code @ControllerGeneration.defaultPageSize} 会被静默丢弃；视为未指定后，调用方经
 *       {@link #hasPageSize()} 判断，再用自己的默认值（生成类的 {@code DEFAULT_PAGE_SIZE}）兜底。</li>
 *   <li>挡住危险的边界值：{@code currentPage <= 0} 会让 {@link PageResult} 构造期抛错（端点 500）；
 *       {@code pageSize < 0} 更糟——MyBatis-Plus 把 {@code size < 0} 当作「不分页、取全量」，原样透传
 *       等于让客户端用一个 {@code -1} 把整表拉走。</li>
 * </ol>
 *
 * <p><b>可变</b>（无参构造 + setter），与不可变的 {@link PageResult} 刻意不同：本类是反序列化目标，
 * Jackson 需要无参构造与 setter；出参信封只在响应方向流动，故取不可变。
 */
public class PageParam {

    /** 页码的库级默认值：第 1 页。 */
    public static final long DEFAULT_CURRENT_PAGE = 1L;

    /** 每页条数的<b>库级</b>默认值。实体级配置见 {@code @ControllerGeneration.defaultPageSize}——
     *  该配置由生成的查询条件类持有；请求未指定 {@code pageSize} 时（见 {@link #hasPageSize()}）
     *  由生成类用自己的 {@code DEFAULT_PAGE_SIZE} 兜底，优先于本值。 */
    public static final long DEFAULT_PAGE_SIZE = 10L;

    private long currentPage;

    private long pageSize;

    public PageParam() {
    }

    /**
     * 以显式取值构造（两个值都按「{@code <= 0} 视为未指定」处理，故传 0 / 负数等同于不指定）。
     */
    public static PageParam of(long currentPage, long pageSize) {
        PageParam param = new PageParam();
        param.setCurrentPage(currentPage);
        param.setPageSize(pageSize);
        return param;
    }

    /**
     * 生效页码，<b>从 1 开始</b>；未指定（{@code <= 0}）时为 {@link #DEFAULT_CURRENT_PAGE}。
     */
    public long getCurrentPage() {
        return currentPage > 0 ? currentPage : DEFAULT_CURRENT_PAGE;
    }

    public void setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * 请求是否显式给出了正的 {@code pageSize}（{@code > 0}）。未指定时调用方应用自己的默认
     * （生成类的实体级 {@code DEFAULT_PAGE_SIZE}），不要直接用 {@link #getPageSize()}——后者回落的是库级 10。
     */
    public boolean hasPageSize() {
        return pageSize > 0;
    }

    /**
     * 生效每页条数；未指定（{@code <= 0}）时为<b>库级</b>默认 {@link #DEFAULT_PAGE_SIZE}。
     * 需要让实体级配置优先时先看 {@link #hasPageSize()}。
     */
    public long getPageSize() {
        return resolvePageSize();
    }

    /**
     * 生效每页条数；未指定（{@code <= 0}）时回落 {@link #DEFAULT_PAGE_SIZE}。
     * <b>返回值恒 {@code > 0}</b>（{@link #DEFAULT_PAGE_SIZE} 为正）。
     *
     * <p>需要实体级 {@code @ControllerGeneration.defaultPageSize} 优先时：先 {@link #hasPageSize()}，
     * 为 false 再用生成类自己的 {@code DEFAULT_PAGE_SIZE}（见 {@code {P}{E}PageQuery#pageSize()}）。
     */
    public long resolvePageSize() {
        return pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "PageParam{currentPage=" + getCurrentPage() + ", pageSize=" + getPageSize() + "}";
    }
}
