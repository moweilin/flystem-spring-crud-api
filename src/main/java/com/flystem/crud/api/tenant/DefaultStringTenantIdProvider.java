package com.flystem.crud.api.tenant;

/**
 * 字符串租户值的 {@link DefaultTenantIdProvider}（公开 non-sealed marker，与实体侧
 * {@link StringTenantId} 镜像）：字符串多租户实体生成的 ServiceFactory 无参入口注入本类型。
 * 默认实现 {@link DefaultStringTenantIdProviderImpl}（空字符串）；自定义提供者实现本接口即可替换。
 */
public non-sealed interface DefaultStringTenantIdProvider extends DefaultTenantIdProvider<String> {
}
