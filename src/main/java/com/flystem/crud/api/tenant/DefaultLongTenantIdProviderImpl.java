package com.flystem.crud.api.tenant;

/**
 * {@link DefaultLongTenantIdProvider} 的默认实现（ADR 0005）：返回零 Long。
 *
 * <p>这是数据层约定：默认租户的数据实际存储在 0L 之下，日后更改约定需要数据迁移。
 */
public final class DefaultLongTenantIdProviderImpl implements DefaultLongTenantIdProvider {

    /** 零 Long：Long 类型的默认租户哨兵值。 */
    public static final long ZERO_LONG = 0L;

    @Override
    public Long getDefaultTenantId() {
        return ZERO_LONG;
    }
}
