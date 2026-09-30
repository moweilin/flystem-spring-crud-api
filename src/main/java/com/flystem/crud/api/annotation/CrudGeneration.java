package com.flystem.crud.api.annotation;

import com.flystem.crud.api.enums.GenerationMode;
import com.flystem.crud.api.enums.PackageLayout;
import com.flystem.crud.api.enums.TenantIdType;
import com.flystem.crud.api.id.IdGenerator;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 包级生成配置注解（ADR 0009）：标注于 {@code package-info.java}，作为包内实体生成配置的
 * 默认级。
 *
 * <p>配置分三组组织：实体范围圈定——{@code scanPackage} / {@code includes} /
 * {@code excludes}（包级独有，参与集 = 包扫描集（本包及子孙包，可关）∪ 白名单 − 黑名单，
 * {@link Domain} 无此三键）；跨层全局项——生成范围、实体根包、
 * 产物包布局、命名前缀、主键生成器（从粗到细：先定生成面，再定包结构与类命名，再定 create
 * 时主键来源），以及两项<b>包级独有</b>的领域全局项——全局租户值类型（{@code tenantIdType}，全项目
 * 唯一）与用户主体类型（{@code principalClass}，端点与 Service 方法的用户参数类型，ADR 0018）——为本
 * 注解的直接属性；每个产物层有独立配置注解（如 {@link ControllerGeneration}），作为对应属性嵌套
 * 其中，声明该层的裁剪（是否生成）与 api/impl 归属；层专属选项作为层配置的内部嵌套子配置承载（如
 * Controller 层的 {@code security}），随产物细节在后续工作包补充。层属性按请求链排序——暴露入口
 * 与入参（controller / form / mcp / admin / guest）→ 业务（service / serviceImpl）→ 数据访问（dataAccess）
 * → 数据形态与转换（dto / mapStruct）→ 装配（springAutoConfiguration）。
 *
 * <p>生成引擎将 package-info.java 纳入引导编译，以 ASM 读取编译后 {@code package-info.class}
 * 上的注解值——javac 已完成类型检查与常量折叠，因此配置值支持常量引用
 * （如 {@code prefix = Constants.PREFIX}，须为编译期常量）。
 *
 * <p>配置分两级：本注解（包级）为默认，实体级 {@link Domain} 的配置属性逐项覆盖同名配置项
 * ——具体优先于一般，实体级未显式书写的配置项回落到此。
 *
 * <p>属性与 {@link Domain} 同键、同默认值（包级独有的 {@code scanPackage} /
 * {@code includes} / {@code excludes}（实体范围圈定无实体级语义）、{@code tenantIdType}
 * （全局租户值类型无实体级语义）与 {@code principalClass}（用户主体类型无实体级语义）除外），语义见各属性
 * 及对应类型的 javadoc。
 *
 * @see Domain 实体级生成配置（两级配置的覆盖级）
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface CrudGeneration {

    /** 是否包含默认的包扫描实体（包级独有）：默认 true——本包及子孙包内全部被识别实体参与生成；置 false 则包扫描集不参与，仅 {@link #includes} 显式列出的实体生成（两者皆空 = 本包不生成）。落在更近的已配置子包（该子包自身有 {@code @CrudGeneration}）之下的实体由该子包认领，不被本包扫描越权。 */
    boolean scanPackage() default true;

    /** 参与生成的实体白名单（包级独有）：列出的实体参与生成，与包扫描集（{@link #scanPackage}）取并集；默认空 = 不额外添加。 */
    Class<?>[] includes() default {};

    /** 参与生成的实体黑名单（包级独有）：列出的实体不参与生成；默认空 = 不排除；与 {@link #includes} 重叠时优先——两者都列出的类被排除。 */
    Class<?>[] excludes() default {};

    /** 生成范围：默认 api 与 impl 同时生成，亦可单侧独立生成（见 {@link GenerationMode}）。 */
    GenerationMode mode() default GenerationMode.BOTH;

    /** 是否每实体单独创建根包（以实体名小写命名，挂在认领包下）：默认否。认领包 = 带 {@code @CrudGeneration} 的包（子树扫描 / includes 跨包时仍用配置方包名）。 */
    boolean packagePerEntity() default false;

    /** 产物包布局：默认 {@link PackageLayout#LAYERED}（各产物层落在 {@code generationPackage} 下的独立子包，跨层引用自动补 import）；{@link PackageLayout#FLAT} 时同一实体全部产物同包。与 {@link #packagePerEntity} 正交可叠加。 */
    PackageLayout packageLayout() default PackageLayout.LAYERED;

    /** 命名前缀：全部产物层类名统一携带（如 {@code FUserService}），可置空串关闭（ADR 0016）。 */
    String prefix() default "F";

    /**
     * 主键生成器 bean 类型（可被 {@link Domain#idGenerator} 覆盖）：默认 {@link Void} = <b>由框架
     * / 数据库自生成</b>（产物不注入、create 不赋值，交给 MyBatis-Plus 雪花 / {@code ASSIGN_UUID}
     * 等）。配实现 {@link IdGenerator} 的类（或接口本身）时，ServiceImpl 注入该类型 bean，create
     * 在实体 id 为空时调用 {@link IdGenerator#nextId(Class)}。库提供 {@code UuidIdGenerator} 与
     * {@code UuidV7IdGenerator}（仅当 classpath 有 uuid-creator 的 {@code UuidCreator} 时可用），
     * 见 ADR 0026。
     */
    Class<?> idGenerator() default Void.class;

    /**
     * 全局租户值类型（<b>包级独有</b>，{@link Domain} 无此键、不可按实体覆盖）：默认
     * {@link TenantIdType#UUID}。生成器汇总各包的显式声明与各多租户实体 marker 蕴含的类型，
     * 出现两个及以上不同取值即报错（全局唯一，见 {@link TenantIdType}）。presence-based：
     * 未显式书写不算声明，不参与冲突判定（ADR 0009）。
     */
    TenantIdType tenantIdType() default TenantIdType.UUID;

    /**
     * 端点与 Service 方法的<b>用户主体类型</b>（<b>包级独有</b>，{@link Domain} 无此键）：默认
     * Spring Security 的 {@link UserDetails}。本键配置的是<b>类型</b>；生成代码里的参数名仍为
     * {@code user}。租户面 Controller 的端点方法以本类型为第一个参数（经 {@code @AuthenticationPrincipal}
     * 注入）；Service 层同时提供「携带」与「不携带」本类型参数的两套重载，端点按
     * {@link ControllerGeneration#userlessOperations()} 选调（ADR 0018）。
     *
     * <p>可覆盖为项目自有的 principal 类型（如 {@code LoginUser} / {@code JwtPrincipal}）——凡能被
     * {@code @AuthenticationPrincipal} 注入、且多租户时能被 {@code TenantIdExtractor} 解析出租户值
     * 的类型皆可。
     *
     * <p><b>依赖后果</b>：默认值是 {@code UserDetails.class}，该类型引用会编入本注解的
     * {@code AnnotationDefault} 属性，故<b>任何编译 {@code package-info.java}（标注本注解）的项目
     * 都必须在编译 classpath 上能解析 spring-security-core</b>——即使单租户、即使不使用用户参数。
     * 这是选 {@code Class<?>} 而非 FQCN 字符串载体的代价（ADR 0018 记录了权衡）。
     */
    Class<?> principalClass() default UserDetails.class;

    /** Controller 层独立配置（默认生成、归 impl）。 */
    ControllerGeneration controller() default @ControllerGeneration;

    /** Form 层独立配置（web 输入表单对象，默认生成、归 impl）。 */
    FormGeneration form() default @FormGeneration;

    /** MCP 工具暴露层独立配置（将 CRUD 操作暴露为 MCP 工具，默认关闭——opt-in，需使用方自引 MCP 服务端依赖；归 impl）。 */
    McpGeneration mcp() default @McpGeneration;

    /** 管理端 Controller 层独立配置（管理面端点：多租户实体跨租户、单租户实体默认亦覆盖，默认关闭——opt-in；归 impl）。 */
    AdminControllerGeneration admin() default @AdminControllerGeneration;

    /** Guest（第三方）Controller 层独立配置（与系统用户租户面同数据边界，默认关闭——opt-in；归 impl）。 */
    GuestControllerGeneration guest() default @GuestControllerGeneration;

    /** Service 接口层独立配置（默认生成、归 api）；ServiceFactory 归并于本层，随其生成与归属（ADR 0004）。 */
    ServiceGeneration service() default @ServiceGeneration;

    /** Service 实现层独立配置（默认生成、归 impl）。 */
    ServiceImplGeneration serviceImpl() default @ServiceImplGeneration;

    /** 数据访问层独立配置（Repository / Mapper，默认生成、归 impl）。 */
    DataAccessGeneration dataAccess() default @DataAccessGeneration;

    /** DTO 层独立配置（默认生成、归 api）。 */
    DtoGeneration dto() default @DtoGeneration;

    /** MapStruct 转换层独立配置（默认关闭——opt-in，需使用方引入 mapstruct 依赖；归 impl）。 */
    MapStructGeneration mapStruct() default @MapStructGeneration;

    /** Spring 自动配置层独立配置（自动配置类，默认生成、归 impl）。 */
    SpringAutoConfigurationGeneration springAutoConfiguration() default @SpringAutoConfigurationGeneration;
}
