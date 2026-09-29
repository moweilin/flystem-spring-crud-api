package com.flystem.crud.api.enums;

/**
 * 异步导入 / 导出任务的可见性范围粒度（ADR 0022）：决定生成的工厂方法
 * {@code taskScope(user)} 产出什么非空字符串，供 {@code CrudTaskManager} 的 scope 形参使用。
 *
 * <p>配置入口：{@link com.flystem.crud.api.annotation.ServiceGeneration#taskScope()}（包级
 * {@code @CrudGeneration} / 实体级 {@code @Domain} 可覆盖）。默认 {@link #USER}——按用户隔离任务；
 * 同租户下不同用户互不可见。需要「同租户共享任务台」时显式改为 {@link #TENANT}（仅多租户实体合法）。
 *
 * <p>{@code taskScope(user)} <b>永不返回 {@code null}</b>：解析不出即抛错，不静默回落。
 */
public enum TaskScope {

    /**
     * 用户级：取主体 {@code getUsername()}（要求 {@code principalClass} 具备该方法；默认
     * {@code UserDetails} 满足）。{@code null} / blank → 运行期抛 {@link IllegalArgumentException}。
     */
    USER,

    /**
     * 租户级：取当前租户值的字符串形式（与 {@code getService(user)} 同源，经
     * {@code TenantIdExtractor}）。仅多租户实体可配；单租户配了即生成期报错。
     */
    TENANT
}
