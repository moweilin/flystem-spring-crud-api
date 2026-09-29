package com.flystem.crud.api.annotation;

import com.flystem.crud.api.enums.CrudOperation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 第三方（Guest）面 Controller 产物层的独立配置：与系统用户租户面 {@link ControllerGeneration}
 * 并列，供第三方集成消费。数据边界与租户面相同（多租户仍经主体解析租户、走带租户谓词的
 * {@code GuestService}）；独立 DTO / Service / Controller，经
 * {@code ServiceFactory#getGuestService*} 取得（ADR 0025）。
 *
 * <p>默认关闭（opt-in）：扩大对外暴露面须显式开启。仅实现
 * {@link com.flystem.crud.api.guest.UUIDGuestId} / {@link com.flystem.crud.api.guest.StringGuestId} /
 * {@link com.flystem.crud.api.guest.LongGuestId}（须含约定 {@code guestId} 字段与 {@code getGuestId()}）
 * 的实体才会实际生成 Guest 产物（未实现则跳过并告警，判定方式同 {@code TenantId} marker）。端点隔离于统一路径前缀
 * （{@link #pathPrefix} 默认 {@code /guest}）；防护经 {@link #security}（复用
 * {@link ControllerSecurity}）。
 *
 * <p>仅作为 {@link CrudGeneration}（包级默认）/ {@link Domain}（实体级覆盖）配置属性的
 * 嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface GuestControllerGeneration {

    /** 是否生成本层（裁剪开关）：默认关——opt-in。 */
    boolean enabled() default false;

    /** 是否归入 api 产物：Guest 默认归 impl；需跨模块消费接口时置 true。 */
    boolean api() default false;

    /**
     * Guest Controller 暴露的操作集：默认空 = 全部操作（{@link CrudOperation} 增补新常量亦自动暴露）；
     * 收窄后仅列出的操作生成 Guest 端点。约束：须包含 {@link #guestOnlyOperations} 列出的操作
     * （否则该操作任何面均不生成，fail-safe）。
     */
    CrudOperation[] operations() default {};

    /**
     * 仅 Guest 允许的操作（从系统用户租户面移除）：列出的操作不在常规租户面暴露，仅经本层提供；
     * 默认空 = 不额外限制。落点条件：本层开启——无落点时列出的操作在任何面均不生成（fail-safe）。
     */
    CrudOperation[] guestOnlyOperations() default {};

    /**
     * Guest 端点的统一 URL 前缀：默认 {@code /guest}——将第三方端点隔离在可辨识路径下，
     * 便于网关 / 安全层按前缀集中防护。
     */
    String pathPrefix() default "/guest";

    /**
     * 安全子配置（复用 {@link ControllerSecurity}）：是否为 Guest 端点附加安全注解。默认关（opt-in）。
     * Guest 面忽略 {@link ControllerSecurity#adminOnlyWrites()}（与管理面同：写侧亦用 {@code expression}）。
     */
    ControllerSecurity security() default @ControllerSecurity;
}
