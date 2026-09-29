package com.flystem.crud.api.tenant;

import java.util.UUID;

/**
 * UUID 租户值的 {@link DefaultTenantIdProvider}（公开 non-sealed marker，与实体侧
 * {@link UUIDTenantId} 镜像）：UUID 多租户实体生成的 ServiceFactory 无参入口注入本类型。
 * 默认实现 {@link DefaultUUIDTenantIdProviderImpl}（零 UUID）；自定义提供者实现本接口即可替换。
 */
public non-sealed interface DefaultUUIDTenantIdProvider extends DefaultTenantIdProvider<UUID> {
}
