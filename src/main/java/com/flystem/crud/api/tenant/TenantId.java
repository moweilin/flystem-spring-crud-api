package com.flystem.crud.api.tenant;

/**
 * 多租户标记根接口（ADR 0010，sealed）：实体实现其公开 marker（{@link UUIDTenantId} /
 * {@link StringTenantId} / {@link LongTenantId}）即为多租户实体，未实现即为单租户实体；该判定直接驱动
 * ServiceFactory 的生成形态（ADR 0004）。sealed + permits 将租户值类型约束为仅
 * {@code UUID}、{@code String} 或 {@code Long}，编译期强制。
 *
 * <p>租户值类型由所实现的 marker 固定（{@code UUIDTenantId} → {@code UUID}，
 * {@code StringTenantId} → {@code String}，{@code LongTenantId} → {@code Long}），生成引擎按 marker 读取：决定默认租户
 * 哨兵值的形态（零 UUID / 空字符串 / 零 Long，ADR 0005）与 {@code getService(tenantId)} 的参数类型。
 * 该类型<b>全项目唯一</b>：包级 {@code @CrudGeneration.tenantIdType} 的显式声明与各实体 marker
 * 蕴含的类型必须收敛到同一取值，出现两个及以上不同取值时生成器报错（见 {@code TenantIdType}）。
 *
 * <p>本接口是<b>只读</b>契约：只要求 {@link #getTenantId()}，从类型系统上保证多租户实体确实携带
 * 租户字段（而非注解声明与实际结构脱节）。写侧<b>不设</b> {@code setTenantId}——生成的 impl 产物
 * 在写入路径按<b>实体具体类型</b>填充租户值（{@code entity.setTenantId(...)}，ADR 0013），不经本
 * 接口，故无需在此强制可变性：只读形态的实体同样可标记为多租户。代价是租户写入途径不再有接口
 * 保证，实体须自行提供（setter 或等价途径），否则产物编译失败——错误显式暴露在生成代码上，不静默。
 *
 * @param <T> 租户值类型，{@code UUID}、{@code String} 或 {@code Long}
 */
sealed interface TenantId<T> permits LongTenantId, StringTenantId, UUIDTenantId {

    /**
     * 返回实体当前的租户标识。
     */
    T getTenantId();
}