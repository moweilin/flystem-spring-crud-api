package com.flystem.crud.api.annotation;

import com.flystem.crud.api.enums.CrudOperation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 产物层的独立配置：REST 控制器（类名 = 前缀 + 实体名 + {@code Controller}）。
 *
 * <p>层专属选项：{@link #pathPrefix}（端点统一 URL 前缀，默认空 = 不加前缀）、
 * {@link #defaultPageSize}（分页查询的默认每页条数，默认 10）、
 * {@link #resultWrapper}（端点统一结果包装类，默认 {@code Void.class} = 不包装）、
 * {@link #userlessOperations}（免用户主体的操作集，仅单租户实体可用，默认空 = 全部操作都带用户
 * 参数，ADR 0018）、{@link ControllerSecurity security}（端点安全注解，嵌套子配置，默认关 opt-in）。
 * MCP 工具暴露为同级独立产物层，见 {@link McpGeneration}。
 *
 * <p>仅作为 {@link CrudGeneration}（包级默认）/ {@link Domain}（实体级覆盖）配置属性的
 * 嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ControllerGeneration {

    /** 是否生成本层（裁剪开关）：默认生成。 */
    boolean enabled() default true;

    /** 是否归入 api 产物：Controller 默认归 impl 侧。 */
    boolean api() default false;

    /** 租户面 Controller 端点的统一 URL 前缀：默认空 = 不加前缀（实体路径由 impl 侧按惯例确定）；可覆盖为统一前缀（如 {@code /api}、{@code /v1}）将租户面端点隔离在可辨识路径下，与管理端 {@link AdminControllerGeneration#pathPrefix()} 同机制、不同面。前缀与实体路径的拼接规则在 impl 侧工作包确定（work-breakdown #6 开放点）。 */
    String pathPrefix() default "";

    /**
     * 分页查询的默认每页条数：生成的分页查询条件类 {@code {P}{E}PageQuery}（LIST / PAGE 共用）
     * 以本值初始化 {@code pageSize} 字段，故客户端 POST 空条件体 {@code {}} 即取第 1 页、本值条；
     * 请求体显式给出<b>正数</b>则以请求为准，非正数回落本默认值（见生成产物的 {@code getPageSize()}）。
     *
     * <p>本键位于 Controller 层配置下，但决定的是<b>查询条件类</b>（DTO 层产物）的字段初值——因为
     * 「一页多大」属端点契约。包级 {@code @CrudGeneration} 设默认、实体级 {@code @Domain} 可逐项
     * 覆盖（ADR 0009）。页码 {@code currentPage} <b>从 1 开始</b>（与 {@code PageResult} 同基准，
     * ADR 0019），非本键所辖、不可配。
     */
    int defaultPageSize() default 10;

    /**
     * 端点<b>统一结果包装</b>（ADR 0020）。仅两种取值：
     *
     * <ol>
     *   <li><b>不包装</b>（默认 {@code Void.class}）：端点直接返回数据本身；</li>
     *   <li><b>SPI 形态</b>——配置的类（直接或间接）实现
     *       {@link com.flystem.crud.api.wrap.ResultWrapper}：产物注入该 SPI bean，每个端点写
     *       {@code resultWrapper.wrap(() -> 业务调用)}，信封恒为 {@code -api} 的
     *       {@link com.flystem.crud.api.wrap.R}。参数是 {@code Supplier} 而非<b>已算好的值</b>，故实现
     *       能在业务调用<b>前后</b>插入逻辑（异常兜底、耗时统计、日志……）而无需改产物。默认实现
     *       {@link com.flystem.crud.api.wrap.DefaultResultWrapper} 由
     *       {@code FlystemSpringCrudAutoConfiguration} 注册，声明自己的 {@code ResultWrapper} bean
     *       即整体替换。</li>
     * </ol>
     *
     * <p>配了非 {@code Void} 却未实现 {@code ResultWrapper} → 生成期<b>报错</b>，不产出编译不过的
     * 代码。类字面量在注解字节码里是 raw type，「是否实现 SPI」只能读字节码判定（沿接口层级与父类链
     * 递归）。
     *
     * <p>用 {@code Class} 而非全限定名字符串：配置错误能在编译期暴露、IDE 可导航可重构（同
     * {@code principalClass} 的取舍，ADR 0018）。与 {@code principalClass} 不同的是，默认值
     * {@code Void.class} 属 {@code java.lang}，<b>不会</b>给使用方强加任何依赖。
     *
     * <p>包装后的连带变化：{@code count} / {@code exists} 的返回由基本类型 {@code long} /
     * {@code boolean} <b>装箱</b>为 {@code Long} / {@code Boolean}（泛型实参不能是基本类型）；
     * 不包装时端点也一律返回引用类型，故包装与否只差外层信封。
     */
    Class<?> resultWrapper() default Void.class;

    /**
     * 无需用户主体的操作集：列出的操作，其端点方法<b>不带</b>用户参数，并调用 Service 层不携带
     * 用户参数的重载；默认空 = 全部操作都带用户参数（ADR 0018）。
     *
     * <p><b>仅适用于单租户实体</b>：多租户实体的当前租户来自用户主体（ADR 0017），列出任何操作
     * 都会在<b>生成时报错</b>——无用户即无租户边界，静默回落默认租户会把越权访问伪装成正常数据。
     *
     * <p>本键位于 Controller 层配置下，但决定 Service 层对应操作由哪一套重载消费（两套重载都会
     * 生成，见 ADR 0018）。管理端 Controller 的端点<b>恒</b>带用户参数，无本键。
     */
    CrudOperation[] userlessOperations() default {};

    /** 安全子配置：是否为端点附加安全注解（默认关——opt-in，需 Spring Security 依赖）。 */
    ControllerSecurity security() default @ControllerSecurity;
}
