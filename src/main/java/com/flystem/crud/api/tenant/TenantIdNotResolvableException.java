package com.flystem.crud.api.tenant;

/**
 * 当前租户解析失败（运行期）：认证主体既未实现 {@link TenantId} 的公开 marker
 * （{@code UUIDTenantId} / {@code StringTenantId} / {@code LongTenantId}，ADR 0010）、也没有
 * public {@code getTenantId()}，或取到的值与目标类型不兼容 / 为空。
 *
 * <p><b>刻意不回落</b>到默认租户哨兵值（ADR 0005）：静默回落会把越权访问伪装成正常数据——
 * 请求会落进默认租户并看似成功。生成的多租户 Controller 端点在解析失败时即以本异常终止请求，
 * 由使用方的异常处理（如 {@code @ControllerAdvice}）决定对外形态。
 *
 * <p>本异常不携带租户值与主体内容（只含类型名），避免把敏感数据写进日志。
 */
public class TenantIdNotResolvableException extends RuntimeException {

    public TenantIdNotResolvableException(String message) {
        super(message);
    }

    public TenantIdNotResolvableException(String message, Throwable cause) {
        super(message, cause);
    }
}