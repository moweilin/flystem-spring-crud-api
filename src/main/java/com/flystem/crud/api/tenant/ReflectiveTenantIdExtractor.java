package com.flystem.crud.api.tenant;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * {@link TenantIdExtractor} 的默认实现：不要求使用方为本库接口改动自己的 {@code UserDetails}。
 * 由 {@code FlystemSpringCrudAutoConfiguration} 在使用方未声明同类型 bean 时注册；声明自己的
 * {@code TenantIdExtractor} bean 即整体替换本实现。
 *
 * <p>可识别的主体形态（按序尝试，命中即返回）：
 * <ol>
 *   <li>实现 {@link TenantId} 的公开 marker（{@code UUIDTenantId} / {@code StringTenantId} /
 *       {@code LongTenantId}，ADR 0010）→ 调 {@code getTenantId()}；</li>
 *   <li>提供 public {@code getTenantId()} 方法（鸭子类型，反射调用）。</li>
 * </ol>
 *
 * <p>类型转换：原始值为 {@code String} 而目标为 {@code UUID} / {@code Long} 时按字面解析（JWT claim
 * 常为字符串形态）；解析失败或类型不兼容即抛，不做其它猜测。全局租户值类型唯一
 * （{@code @CrudGeneration.tenantIdType}，ADR 0017），故正常配置下主体形态与目标类型天然一致，
 * 转换只是给字符串形态的凭据留的便利。
 *
 * <p>解析不出一律抛 {@link TenantIdNotResolvableException}，<b>不回落</b>默认租户哨兵值（ADR 0005）。
 * 异常消息只带类型名、不回显主体内容与租户值（可能敏感）。
 */
public final class ReflectiveTenantIdExtractor implements TenantIdExtractor {

    /** 鸭子类型访问器名：与 {@link TenantId#getTenantId()} 同名，未实现本库接口的主体亦可被识别。 */
    private static final String ACCESSOR = "getTenantId";

    /** 解析失败时附带的形态提示（避免使用方去翻文档）。 */
    private static final String HINT = "；主体须实现 UUIDTenantId / StringTenantId / LongTenantId 之一"
            + "（ADR 0010），或提供 public getTenantId()";

    @Override
    public <T> T extract(UserDetails userDetails, Class<T> type) {
        return convert(tenantIdOf(userDetails), type);
    }

    /** 主体 → 原始租户值；形态不符即抛（本实现只有这一个来源，无处可退）。 */
    private static Object tenantIdOf(UserDetails userDetails) {
        if (userDetails == null) {
            throw new TenantIdNotResolvableException("userDetails is null" + HINT);
        }
        if (userDetails instanceof TenantId<?> marker) {
            return marker.getTenantId();
        }
        Method accessor;
        try {
            accessor = userDetails.getClass().getMethod(ACCESSOR);
        } catch (NoSuchMethodException e) {
            throw new TenantIdNotResolvableException(
                    "cannot resolve tenantId from userDetails: " + userDetails.getClass().getName() + HINT);
        }
        try {
            return accessor.invoke(userDetails);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new TenantIdNotResolvableException(
                    "getTenantId() invocation failed on " + userDetails.getClass().getName(), e);
        }
    }

    /** 原始值 → 目标类型：同型直返；String → UUID / Long 按字面解析；其余不兼容即抛（不静默兜底）。 */
    private static <T> T convert(Object raw, Class<T> type) {
        if (raw == null) {
            throw new TenantIdNotResolvableException("resolved tenantId is null（主体的租户值为空）");
        }
        if (type.isInstance(raw)) {
            return type.cast(raw);
        }
        if (raw instanceof String text) {
            String trimmed = text.trim();
            try {
                if (type == UUID.class) {
                    return type.cast(UUID.fromString(trimmed));
                }
                if (type == Long.class) {
                    return type.cast(Long.valueOf(trimmed));
                }
            } catch (IllegalArgumentException e) {
                throw new TenantIdNotResolvableException(
                        "cannot parse tenantId as " + type.getName() + ": " + trimmed, e);
            }
        }
        throw new TenantIdNotResolvableException("tenantId type mismatch: resolved "
                + raw.getClass().getName() + " but " + type.getName() + " is required");
    }
}