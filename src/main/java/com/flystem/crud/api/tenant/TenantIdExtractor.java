package com.flystem.crud.api.tenant;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 当前租户解析（SPI，协议 §4.5）：从 {@link UserDetails} 提取租户值。生成的<b>多租户</b> Controller
 * 以本接口为构造注入依赖；端点第一个参数是<b>用户主体</b>（{@code @AuthenticationPrincipal} 注入，
 * 类型由 {@code @CrudGeneration.principalClass} 配置、默认 {@link UserDetails}，ADR 0018），从该主体取当前租户后
 * 交 ServiceFactory（ADR 0004 / 0013）——取代此前的 {@code X-Tenant-Id} 请求头占位策略：租户来自
 * <b>已认证的身份</b>而非客户端可任意填写的请求头。
 *
 * <p><b>实现契约只有一个方法</b>：{@link #extract(UserDetails, Class)}。从 {@link Authentication}
 * 取 {@link UserDetails} 的活由 {@link #userDetails(Authentication)} 静态方法承担、并作为
 * {@code default} 方法接好，自定义实现无需重复处理认证信息的形态差异。
 *
 * <p>默认实现 {@link ReflectiveTenantIdExtractor}（识别两种主体形态：实现 {@link TenantId} 的公开
 * marker，或提供 public {@code getTenantId()}）由 {@code FlystemSpringCrudAutoConfiguration} 在
 * <b>使用方未声明同类型 bean 时</b>注册（{@code @ConditionalOnMissingBean}）——声明自己的
 * {@code TenantIdExtractor} bean 即整体替换（如从 JWT claim、租户上下文或数据库取租户）。
 *
 * <p><b>不静默兜底</b>：解析不出应抛 {@link TenantIdNotResolvableException}，绝不回落到默认租户
 * 哨兵值（ADR 0005）——静默回落会让越权访问看起来像正常数据。自定义实现同样应遵守。
 *
 * <p>依赖：本接口引用 Spring Security 类型，{@code -api} 以 {@code compileOnly} 声明——不进发布
 * POM、不传递给使用方。<b>仅多租户 Controller 产物</b>引用本接口与 {@code Authentication}，使用方
 * 需自引 {@code spring-security-core}（与 {@code @ControllerSecurity} 同一套 opt-in 约定，
 * ADR 0008 非侵入）；单租户产物不引用，无需该依赖。所用 API 在 Spring Security 6 与 7 同形，
 * Boot 3 / Boot 4 两代通用（ADR 0012）。
 */
public interface TenantIdExtractor {

    /**
     * 从用户信息提取当前租户并转为目标类型。
     *
     * @param userDetails 认证主体（非 null；由 {@link #extract(Authentication, Class)} 保证）
     * @param type        目标租户值类型（{@code UUID.class} / {@code String.class} / {@code Long.class}）
     * @throws TenantIdNotResolvableException 主体形态不符、租户值为空或类型不兼容
     */
    <T> T extract(UserDetails userDetails, Class<T> type);

    /**
     * 从认证信息提取当前租户（便利入口，供自行持有 {@code Authentication} 的场合使用）：取 principal
     * 中的 {@link UserDetails} 后委派 {@link #extract(UserDetails, Class)}。自定义实现通常无需覆盖——
     * 生成的端点已直接拿到用户主体，走 {@link #extract(UserDetails, Class)}。
     *
     * @throws TenantIdNotResolvableException 认证信息缺失、principal 不是 {@link UserDetails}，
     *                                        或委派实现解析失败
     */
    default <T> T extract(Authentication authentication, Class<T> type) {
        return extract(userDetails(authentication), type);
    }

    /**
     * 取认证信息里的用户信息（{@code getPrincipal()} 为 {@link UserDetails} 时）——端点需要用户
     * 身份本体（而非仅租户值）时亦用本方法。
     *
     * @throws TenantIdNotResolvableException 认证信息为 null，或 principal 不是 {@link UserDetails}
     *                                        （如匿名认证下 principal 为用户名字符串）
     */
    static UserDetails userDetails(Authentication authentication) {
        if (authentication == null) {
            throw new TenantIdNotResolvableException("authentication is null（端点未经认证？）");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails details) {
            return details;
        }
        // 只报类型名、不回显值：principal 可能是用户名字符串等敏感内容
        throw new TenantIdNotResolvableException("authentication principal is not a UserDetails: "
                + (principal == null ? "null" : principal.getClass().getName()));
    }
}