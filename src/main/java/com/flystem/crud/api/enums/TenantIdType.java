package com.flystem.crud.api.enums;

/**
 * 全局租户值类型（CONTEXT.md「全局租户值类型」）：本项目多租户实体的租户标识取值类型。
 *
 * <p><b>全局唯一，不可按实体覆盖</b>：只在包级 {@link com.flystem.crud.api.annotation.CrudGeneration#tenantIdType()} 声明，
 * {@link com.flystem.crud.api.annotation.Domain} <b>没有</b>此键——同一次生成里混用多种租户值类型意味着数据面边界不一致
 * （默认租户哨兵值形态、{@code getService(tenantId)} 参数类型、运行期租户解析都要按同一类型
 * 对齐）。生成器汇总「各包的显式声明」（presence-based，ADR 0009：未书写不算声明）与「各多租户
 * 实体 marker（{@code UUIDTenantId} / {@code StringTenantId} / {@code LongTenantId}，ADR 0010）
 * 蕴含的类型」，出现两个及以上不同取值即<b>报错</b>，不猜测、不取多数。
 *
 * <p>与 marker 的分工：marker 仍是「该实体是多租户」的唯一判据（ADR 0010），本枚举声明的是
 * 全项目统一的租户值类型；两者必须一致。
 */
public enum TenantIdType {

    /** 租户值为 {@code java.util.UUID}（默认）；marker {@code UUIDTenantId}，默认租户为零 UUID（ADR 0005）。 */
    UUID,

    /** 租户值为 {@code java.lang.String}；marker {@code StringTenantId}，默认租户为空字符串。 */
    STRING,

    /** 租户值为 {@code java.lang.Long}；marker {@code LongTenantId}，默认租户为零 Long。 */
    LONG
}