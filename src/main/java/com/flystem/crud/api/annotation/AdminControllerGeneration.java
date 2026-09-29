package com.flystem.crud.api.annotation;

import com.flystem.crud.api.enums.CrudOperation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理端 Controller 产物层的独立配置：管理面 HTTP 端点，与租户面
 * {@link ControllerGeneration} 并列，供管理后台消费。多租户实体经本层开放跨租户的
 * 查询与操作能力（租户 Service 经 {@code ServiceFactory.getService(tenantId)} 将数据面
 * 约束在单租户内，ADR 0004）；单租户实体默认亦被覆盖（{@link #includeSingleTenant}）
 * ——无跨租户语义，本层即其统一管理员面。Admin 产物命名为
 * {@code {前缀}{实体}Admin{层}}（与租户面同子包）；跨租户数据面由独立 {@code AdminService}
 * （无租户谓词）经 {@code ServiceFactory#getAdminService*} 提供（ADR 0023）。
 *
 * <p>默认关闭（opt-in）：管理面（尤以跨租户数据面为甚）安全敏感，无条件生成会扩大
 * 攻击面，须显式开启（fail-safe）；端点防护经 {@link #security} 子配置（复用 Controller
 * 安全子配置 {@link ControllerSecurity}），其默认关、强烈建议开启。
 *
 * <p>仅作为 {@link CrudGeneration}（包级默认）/ {@link Domain}（实体级覆盖）配置属性的
 * 嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface AdminControllerGeneration {

    /** 是否生成本层（裁剪开关）：默认关——opt-in，管理面须显式开启。 */
    boolean enabled() default false;

    /** 是否归入 api 产物：管理端默认归 impl 侧管理面（随部署单元），需跨模块消费接口时置 true。 */
    boolean api() default false;

    /** 是否包含单租户实体（未实现 {@code TenantId}、无 tenantId）：默认包含——管理后台统一管理入口，单租户实体的管理端点即其管理员面（无跨租户语义）；置否仅对多租户实体生成。联动：单租户实体经 {@link #adminOnlyOperations} 列出的操作须本键开启方有落点（否则任何面均不生成，fail-safe）。 */
    boolean includeSingleTenant() default true;

    /** 管理端 Controller 暴露的操作集：默认空 = 全部操作（{@link CrudOperation} 增补新常量亦自动暴露）；收窄后仅列出的操作生成管理端点。约束：须包含 {@link #adminOnlyOperations} 列出的操作（否则该操作任何面均不生成，fail-safe 宁缺勿漏）。 */
    CrudOperation[] operations() default {};

    /** 仅管理端允许的操作（从租户面移除的操作，不限租户形态）：列出的操作不在常规租户面暴露（Controller 端点、MCP 工具等不生成对应产物），仅经本层提供；默认空 = 不额外限制。落点条件：本层开启且覆盖该实体（单租户实体需 {@link #includeSingleTenant}，默认开）——无落点时列出的操作在任何面均不生成（fail-safe）。另见 {@link #singleTenantWritesOnAdmin()}：单租户实体默认把写侧并入本集。 */
    CrudOperation[] adminOnlyOperations() default {};

    /**
     * 单租户实体（未实现 {@code TenantId}）写侧是否仅落在管理面：默认 true——本层开启且覆盖该实体时，
     * 自动把写侧操作（{@link CrudOperation#CREATE} / {@link CrudOperation#UPDATE} /
     * {@link CrudOperation#DELETE} / {@link CrudOperation#IMPORT}）并入
     * {@link #adminOnlyOperations}（与显式列表取并集），租户面 Controller 不再生成写端点。
     * 置 false 则不自动并入（可仍用 {@link ControllerSecurity#adminOnlyWrites()} 就地保护租户面写侧）。
     * 多租户实体忽略本键。
     */
    boolean singleTenantWritesOnAdmin() default true;

    /** 管理端 Controller 端点的统一 URL 前缀：默认 {@code /admin}——将管理端端点隔离在可辨识路径下，便于网关 / 安全层按前缀集中防护（fail-safe 取向）。 */
    String pathPrefix() default "/admin";

    /** 安全子配置（复用 {@link ControllerSecurity}，与租户面 Controller 层同型）：是否为管理端点附加安全注解。默认关（opt-in）。生成引擎对管理面的系统默认表达式为 {@code hasRole('ADMIN') and hasAuthority('{entity}:{action}')}（管理员角色与实体操作权限同时要求；仅写 {@code @ControllerSecurity(enabled = true)}、不显式覆盖 {@code expression} 时生效）；强烈建议开启。管理面忽略 {@link ControllerSecurity#adminOnlyWrites()}。 */
    ControllerSecurity security() default @ControllerSecurity;
}
