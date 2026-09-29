package com.flystem.crud.api.tenant;

/**
 * 默认租户 ID 提供者根接口（ADR 0005，sealed）：给出"默认租户"的 ID。多租户实体
 * ServiceFactory 的无参 {@code getService()} 即返回绑定该租户的 Service（ADR 0004）。
 *
 * <p>与实体侧 {@link TenantId} sealed 体系（ADR 0010）镜像：本接口包私有，公开入口为三个
 * non-sealed marker——{@link DefaultUUIDTenantIdProvider}、{@link DefaultStringTenantIdProvider}
 * 与 {@link DefaultLongTenantIdProvider}；租户值类型由 marker 固定，sealed 使"仅 UUID、String
 * 或 Long"编译期强制。生成的 ServiceFactory
 * 按实体租户 marker 注入对应 marker 类型；自定义提供者实现 marker 即可替换（默认实现为
 * {@link DefaultUUIDTenantIdProviderImpl} / {@link DefaultStringTenantIdProviderImpl} /
 * {@link DefaultLongTenantIdProviderImpl}，
 * 加载与替换机制属运行期支撑，work-breakdown #8 落地）。
 *
 * @param <T> 租户值类型，{@code UUID}、{@code String} 或 {@code Long}
 */
sealed interface DefaultTenantIdProvider<T> permits DefaultUUIDTenantIdProvider, DefaultStringTenantIdProvider, DefaultLongTenantIdProvider {

    /**
     * 返回默认租户 ID。
     */
    T getDefaultTenantId();
}
