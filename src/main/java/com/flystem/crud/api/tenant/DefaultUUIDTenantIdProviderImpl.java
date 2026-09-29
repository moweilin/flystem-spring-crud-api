package com.flystem.crud.api.tenant;

import java.util.UUID;

/**
 * {@link DefaultUUIDTenantIdProvider} 的默认实现（ADR 0005）：返回零 UUID。
 *
 * <p>这是数据层约定：默认租户的数据实际存储在零 UUID 之下，日后更改约定需要数据迁移。
 */
public final class DefaultUUIDTenantIdProviderImpl implements DefaultUUIDTenantIdProvider {

    /** 零 UUID：UUID 类型的默认租户哨兵值。 */
    public static final UUID ZERO_UUID = new UUID(0L, 0L);

    @Override
    public UUID getDefaultTenantId() {
        return ZERO_UUID;
    }
}
