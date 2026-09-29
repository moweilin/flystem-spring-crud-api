package com.flystem.crud.api.tenant;

/**
 * {@link DefaultStringTenantIdProvider} 的默认实现（ADR 0005）：返回空字符串。
 *
 * <p>这是数据层约定：默认租户的数据实际存储在空字符串之下，日后更改约定需要数据迁移。
 */
public final class DefaultStringTenantIdProviderImpl implements DefaultStringTenantIdProvider {

    /** 空字符串：字符串类型的默认租户哨兵值。 */
    public static final String EMPTY_TENANT_ID = "";

    @Override
    public String getDefaultTenantId() {
        return EMPTY_TENANT_ID;
    }
}
