package com.flystem.crud.api.enums;

/**
 * CRUD 操作词汇表：实体生成产物（端点 / 方法）的规范操作全集
 * （CONTEXT.md「CRUD 操作词汇表」）。
 *
 * <p><b>不在 {@code annotation} 子包</b>：本类型是<b>枚举</b>而非注解——它被注解引用
 * （{@code CrudOperation[]} 属性），自身却不是注解。故与 {@link GenerationMode} /
 * {@link PackageLayout} / {@link TenantIdType} 同置于 {@code enums} 子包（分包判据见该包的
 * {@code package-info}）。
 *
 * <p>引用方：安全注解粒度（{@link com.flystem.crud.api.annotation.ControllerSecurity#securedOperations()}）；安全表达式
 * {@code {action}} 占位符渲染（枚举名小写，如 {@code create}）；管理端暴露操作集与仅
 * 管理端操作的圈定（{@link com.flystem.crud.api.annotation.AdminControllerGeneration#operations()} /
 * {@link com.flystem.crud.api.annotation.AdminControllerGeneration#adminOnlyOperations()}）；MCP 暴露操作子集
 * （impl 侧工作包接线，work-breakdown #6 / #7）。
 *
 * <p>常量按读写侧划分（判据：是否修改数据面）：{@link #GET} / {@link #LIST} /
 * {@link #COUNT} / {@link #PAGE} / {@link #EXISTS} / {@link #EXPORT} / {@link #META} 为读侧，
 * {@link #CREATE} / {@link #UPDATE} / {@link #DELETE} / {@link #IMPORT} 为写侧——
 * 安全表达式渲染与单租户实体写侧管理员专属的就地保护
 * （{@link com.flystem.crud.api.annotation.ControllerSecurity#adminOnlyWrites()}）依赖该划分；批量导出的数据泄露面大，
 * 由使用方经 {@link com.flystem.crud.api.annotation.ControllerSecurity#securedOperations()} / 表达式覆盖或
 * {@link com.flystem.crud.api.annotation.AdminControllerGeneration#adminOnlyOperations()} 自行收紧。
 *
 * <p>增补常量非破坏（各处"默认空 = 全部"语义自动覆盖新操作，增补时同步标注读写侧），
 * 改名是破坏性变更——impl 侧 Service 方法集设计须与本词汇对齐。
 */
public enum CrudOperation {

    /** 创建（写侧）。 */
    CREATE,

    /** 单条获取（读侧）。 */
    GET,

    /**
     * 列表（读侧）：按查询条件取一页记录，返回<b>裸数组</b>（不带分页元数据；要元数据见
     * {@link #PAGE}）。端点为 {@code POST /{entity}/list}，条件经请求体传入
     * （{@code {P}{E}PageQuery}，含 {@code currentPage} / {@code pageSize}）。
     */
    LIST,

    /** 更新（写侧）。 */
    UPDATE,

    /** 删除（写侧）。 */
    DELETE,

    /** 批量导入（写侧——数据流入、修改数据面）。 */
    IMPORT,

    /**
     * 批量导出（读侧——数据流出、不修改数据面）：按查询条件取<b>全量</b>（不分页）。端点为
     * {@code POST /{entity}/export}，条件经请求体传入（{@code {P}{E}Query}，不带分页字段）。
     */
    EXPORT,

    /**
     * 计数（读侧）：返回满足查询条件的记录数。端点为 {@code POST /{entity}/count}，条件经请求体传入
     * （{@code {P}{E}Query}，与 {@link #EXPORT} 同形——计数针对的是<b>条件命中的全集</b>，不是某一页，
     * 故不带分页字段）。
     */
    COUNT,

    /**
     * 分页查询（读侧）：按查询条件取一页记录并附分页元数据（{@code PageResult}，ADR 0019）。
     * 端点为 {@code POST /{entity}/page}，条件经请求体传入（{@code {P}{E}PageQuery}）。
     */
    PAGE,

    /**
     * 存在性检查（读侧）：是否有记录满足查询条件。端点为 {@code POST /{entity}/exists}，条件经请求体
     * 传入（{@code {P}{E}Query}）。
     *
     * <p><b>不是按主键检查</b>——「某 id 是否存在」由 {@link #GET} 覆盖（不存在即返回 null）。按条件
     * 的存在性检查与 {@link #COUNT} / {@link #EXPORT} 共用同一条件类，语义上才对齐；待查询条件类的
     * 字段条件落地（wb#6），按 id 检查即 {@code exists(条件里 id = X)} 的特例。
     */
    EXISTS,

    /**
     * 实体查询元信息（读侧）：当前实体<b>可查询字段的说明</b>——字段名、Java 类型简名、数据库列名、
     * 是否租户字段，供前端动态渲染查询表单、供 MCP 工具描述参数。返回
     * {@link com.flystem.crud.api.meta.QueryMeta}，端点为 {@code GET /{entity}/meta}。
     *
     * <p>与其它读侧操作的三点不同：① <b>不带查询条件</b>——它描述的是<b>实体</b>而非某个数据子集，
     * 故走 GET、无请求体；② <b>不分租户</b>——各租户看到的字段集相同；③ 由生成的 ServiceImpl 按
     * 生成期已知的实体元数据直接构造，故与实体定义天然同步、不会漂移。
     *
     * <p>「含义 / 显示名」与「可用操作符」两项暂缺：前者待实体侧有标注来源，后者待查询条件类的字段
     * 条件落地（协议 §8 登记为 wb#6），届时与 {@code {P}{E}Query} 的字段一一对应。
     */
    META
}
