package com.flystem.crud.api.annotation;

import com.flystem.crud.api.enums.CrudOperation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 类端点层的安全子配置，租户面（{@link ControllerGeneration#security()}）与
 * 管理端（{@link AdminControllerGeneration#security()}）共用同一类型：是否为生成的
 * Controller 端点附加安全注解及其生成规则。
 * 注解载体默认为 Spring Security 的 {@code @PreAuthorize}（方法安全），可覆盖为任意
 * 单值注解（见 {@link #authorizeAnnotation}）；表达式经 {@link #expression} 模板渲染——
 * 占位符 {@code {entity}}（实体名小写）、{@code {action}}（{@link CrudOperation}
 * 枚举名小写；操作与端点的映射在 impl 侧工作包确定，work-breakdown #6 开放点）。
 *
 * <p>单租户实体的就地保护亦由本子配置承载：{@link #adminOnlyWrites}（默认开）生效时，
 * 写侧操作以 {@link #adminExpression}、读侧操作以 {@link #expression} 渲染；多租户实体
 * 不适用（租户隔离即数据面边界）。将操作从租户面迁移至管理面的策略见
 * {@link AdminControllerGeneration#adminOnlyOperations()}。
 *
 * <p>默认关闭（opt-in）：产物引用 {@link #authorizeAnnotation} 所属安全框架类型，需
 * 使用方自引依赖，无条件生成会破坏未引入依赖的项目（ADR 0008 非侵入）——
 * {@link #adminOnlyWrites} 的就地保护同样在本开关开启后才渲染到端点。安全机制的启用
 * （如 {@code @PreAuthorize} 需 {@code @EnableMethodSecurity}）与管理员身份的提供由
 * 使用方负责，产物不改变全局安全配置。
 *
 * <p>仅作为 {@link ControllerGeneration} / {@link AdminControllerGeneration}
 * {@code security} 配置属性的嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ControllerSecurity {

    /** 是否为端点附加安全注解：默认关——opt-in，需使用方自引安全框架依赖。 */
    boolean enabled() default false;

    /** 安全注解载体全限定名：默认 Spring Security {@code @PreAuthorize}；可覆盖为任意以 {@code value} 为唯一必需属性的单值注解（如 Shiro {@code @RequiresPermissions}、Sa-Token {@code @SaCheckPermission}、项目自有权限注解），生成器按 {@code @<简名>(<expression 渲染值>)} 产出并补 import，载体依赖由使用方自引。不取 {@code annotationType}：与 {@code java.lang.annotation.Annotation} 自身方法同名，注解属性不得声明。 */
    String authorizeAnnotation() default "org.springframework.security.access.prepost.PreAuthorize";

    /** 安全表达式模板（即载体注解的 {@code value} 值）：占位符 {@code {entity}}（实体名小写）、{@code {action}}（{@link CrudOperation} 枚举名小写，如 {@code create}）；表达式语法随载体——默认 {@code @PreAuthorize} 为 SpEL，默认权限串风格，渲染如 {@code hasAuthority('user:create')}，可覆盖为任意 SpEL（如角色制 {@code hasRole('ADMIN')}）；覆盖载体时通常随之覆盖（如 Shiro 用纯权限串 {@code {entity}:{action}}）。{@link #adminOnlyWrites} 对单租户实体生效时，读侧操作以本模板、写侧操作以 {@link #adminExpression} 渲染。 */
    String expression() default "hasAuthority('{entity}:{action}')";

    /** 附加安全注解的操作集：默认空 = 全部操作均附加（{@link CrudOperation} 增补新常量亦自动保护）；收窄后未列出的操作不加注解，访问控制回落使用方全局安全策略（如 {@code {CREATE, UPDATE, DELETE}} = 豁免读侧）；整体关闭用 {@link #enabled} = false。就地保护作用于收窄后的操作集：仍在集内的写侧操作以 {@link #adminExpression} 渲染。 */
    CrudOperation[] securedOperations() default {};

    /** 单租户实体写侧管理员专属（就地保护）：无 tenantId 实体的写侧操作（CREATE / UPDATE / DELETE / IMPORT）仅管理员权限可执行（以 {@link #adminExpression} 渲染），非管理员仅保留读侧（GET / LIST / COUNT / PAGE / EXISTS / EXPORT，以 {@link #expression} 渲染）；默认开启，可关闭（关闭后全部操作以 {@link #expression} 渲染）。多租户实体不适用（租户隔离即数据面边界）。需 {@link #enabled} 开启后才渲染。 */
    boolean adminOnlyWrites() default true;

    /** 管理员权限表达式：默认 Spring Security 管理员角色风格 {@code hasRole('ADMIN')}，可覆盖为项目管理员语义（如 {@code hasRole('SUPERUSER')}、{@code hasAuthority('system:admin')}）；支持 {@code {entity}} / {@code {action}} 占位符（同 {@link #expression}）；覆盖载体注解时通常随之覆盖。 */
    String adminExpression() default "hasRole('ADMIN')";
}
