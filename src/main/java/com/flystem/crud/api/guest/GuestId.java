package com.flystem.crud.api.guest;

/**
 * Guest 面标记根接口（ADR 0025，sealed）：实体实现其公开 marker（{@link UUIDGuestId} /
 * {@link StringGuestId} / {@link LongGuestId}）才可生成 Guest* 产物；未实现则为无 Guest 面实体。
 * sealed + permits 将 guest 值类型约束为 {@code UUID}、{@code String} 或 {@code Long}。
 *
 * <p>本接口是<b>只读</b>契约：只要求 {@link #getGuestId()}，保证 Guest 实体确实携带 guest 字段。
 * 写侧不设 {@code setGuestId}——与 {@code TenantId} 同理，实体须自行提供写入途径。
 *
 * @param <T> guest 值类型，{@code UUID}、{@code String} 或 {@code Long}
 * @see com.flystem.crud.api.tenant.TenantId
 */
sealed interface GuestId<T> permits LongGuestId, StringGuestId, UUIDGuestId {

    /** 返回实体当前的 guest 标识。 */
    T getGuestId();
}
