package com.flystem.crud.api.tenant;

/**
 * Long 租户值的 {@link DefaultTenantIdProvider}（公开 non-sealed marker，与实体侧
 * {@link LongTenantId} 镜像）：Long 多租户实体生成的 ServiceFactory 无参入口注入本类型。
 * 默认实现 {@link DefaultLongTenantIdProviderImpl}（零 Long）；自定义提供者实现本接口即可替换。
 */
public non-sealed interface DefaultLongTenantIdProvider extends DefaultTenantIdProvider<Long> {
}
