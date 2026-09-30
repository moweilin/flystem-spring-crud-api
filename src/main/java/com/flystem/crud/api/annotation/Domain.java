package com.flystem.crud.api.annotation;

import com.flystem.crud.api.enums.GenerationMode;
import com.flystem.crud.api.enums.PackageLayout;
import com.flystem.crud.api.id.IdGenerator;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * flystem 自有实体注解（ADR 0001 / 0011），两种用法：
 *
 * <ul>
 *   <li>单独标注（无 {@code @Entity} / mybatis-flex {@code @Table} / mybatis-plus
 *       {@code @TableName}）→ 实体走纯 MyBatis 路径，生成的 Mapper 无生态基类；</li>
 *   <li>与上述路径注解同标 → 不改变路径识别（路径由后者决定），仅承载实体级生成配置。</li>
 * </ul>
 *
 * <p>配置属性与包级 {@link CrudGeneration} 同键、同系统默认值（包级独有的 {@code scanPackage} /
 * {@code includes} / {@code excludes} 除外——实体范围圈定无实体级语义；{@code tenantIdType} 与
 * {@code principalClass} 亦为包级独有——全局租户值类型全项目唯一、用户主体类型全应用统一，<b>均不可按
 * 实体覆盖</b>），逐项覆盖包级配置（ADR 0009）：
 * 本注解上<b>显式书写</b>的配置项以实体级为准，未书写的回落包级；逐项覆盖递归进入各嵌套配置
 * （层配置及其子配置——如 Controller 层的 {@code security}）——任何一级未显式
 * 书写的内部属性，同样回落到包级同名内部属性。生成引擎以字节码级
 * 属性在场性判定"显式书写"（javac 不把注解默认值编入 class 文件，嵌套注解同理），故下方
 * 声明的默认值仅供 javac 类型检查与文档参照，不参与覆盖判定。
 *
 * <p>属性顺序与 {@link CrudGeneration} 一致（层属性按请求链排序）。
 *
 * @see CrudGeneration 包级生成配置（两级配置的默认级）
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Domain {

    /** 生成范围：默认 api 与 impl 同时生成，亦可单侧独立生成（见 {@link GenerationMode}）。 */
    GenerationMode mode() default GenerationMode.BOTH;

    /** 是否每实体单独创建根包（以实体名小写命名，挂在认领包下）：默认否。 */
    boolean packagePerEntity() default false;

    /** 产物包布局：默认 {@link PackageLayout#LAYERED}（各产物层落在 {@code generationPackage} 下的独立子包，跨层引用自动补 import）；{@link PackageLayout#FLAT} 时同一实体全部产物同包。与 {@link #packagePerEntity} 正交可叠加。 */
    PackageLayout packageLayout() default PackageLayout.LAYERED;

    /** 命名前缀：全部产物层类名统一携带（如 {@code FUserService}），可置空串关闭（ADR 0016）。 */
    String prefix() default "F";

    /**
     * 主键生成器 bean 类型：默认 {@link Void} = 由框架 / 数据库自生成；配实现 {@link IdGenerator}
     * 的类（或接口本身）时 create 路径注入并赋值（覆盖包级同名项，ADR 0026）。
     */
    Class<?> idGenerator() default Void.class;

    /**
     * 实体资源名：异步任务的 {@code entity} 字段、生成的 {@code {P}{E}Meta#ENTITY}、导出文件名主干、端点路径段共用。
     * 显式配置或留空（由实体简名自动生成）；生效值在生成期<b>强制小写</b>（{@code Locale.ROOT}）。
     */
    String name() default "";

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
