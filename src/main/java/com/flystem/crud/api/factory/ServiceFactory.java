package com.flystem.crud.api.factory;

/**
 * ServiceFactory 基础类型（ADR 0004）：每实体生成的工厂（类名 = 前缀 + 实体名 +
 * {@code ServiceFactory}，如 {@code FUserServiceFactory}）实现本接口，是实体 Service 的
 * 获取入口。
 *
 * <p>单租户实体的工厂仅实现本接口——只有无参入口；多租户实体的工厂实现
 * {@link TenantServiceFactory}，额外提供 {@code getService(tenantId)}。
 *
 * <p>实体 Service 为每实体独立接口、无共享基础类型（ADR 0004），故 {@code S} 无上界约束。
 *
 * @param <S> 实体 Service 接口类型
 */
public interface ServiceFactory<S> {

    /**
     * 返回 Service 实例：单租户实体即其唯一 Service；多租户实体为绑定默认租户的 Service
     * （默认租户值经租户值类型对应的 {@code DefaultUUIDTenantIdProvider} / {@code DefaultStringTenantIdProvider} / {@code DefaultLongTenantIdProvider} marker 给出，ADR 0004 / 0005）。
     */
    S getService();
}
