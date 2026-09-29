package com.flystem.crud.api.factory;

/**
 * 多租户实体的 ServiceFactory 基础类型（ADR 0004）：为实现 {@code TenantId} 的实体生成的
 * 工厂实现本接口——{@code getService(tenantId)} 与无参 {@code getService()}（继承自
 * {@link ServiceFactory}，返回默认租户的 Service）两种入口并存。
 *
 * <p>单租户实体的工厂不实现本接口：带 {@code tenantId} 的入口对其根本不存在，误用无法通过
 * 编译——租户边界由此在编译期由方法签名强制（ADR 0004）。
 *
 * @param <S> 实体 Service 接口类型
 * @param <T> 租户值类型（与实体 {@code TenantId<T>} 的泛型参数一致）
 */
public interface TenantServiceFactory<S, T> extends ServiceFactory<S> {

    /** 返回绑定指定租户的 Service 实例。 */
    S getService(T tenantId);
}
