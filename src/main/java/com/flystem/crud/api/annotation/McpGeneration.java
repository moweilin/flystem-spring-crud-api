package com.flystem.crud.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MCP 工具暴露产物层的独立配置：将实体的 CRUD 操作暴露为 MCP（Model Context
 * Protocol）工具，供 AI 智能体调用。与 Controller 层同级的独立产物层——暴露载体不
 * 必然依附 REST 端点，裁剪亦与 Controller 层正交；暴露载体（如 Spring AI MCP
 * server）、工具命名与暴露操作子集在 impl 侧工作包确定（work-breakdown #6 开放点）。
 *
 * <p>默认关闭（opt-in）：需使用方自引 MCP 服务端依赖，无条件生成会破坏未引入依赖的
 * 项目（ADR 0008 非侵入）。
 *
 * <p>仅作为 {@link CrudGeneration}（包级默认）/ {@link Domain}（实体级覆盖）配置属性的
 * 嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface McpGeneration {

    /** 是否生成本层（裁剪开关）：默认关——opt-in，需使用方自引 MCP 服务端依赖。 */
    boolean enabled() default false;

    /** 是否归入 api 产物：MCP 工具暴露层默认归 impl 侧。 */
    boolean api() default false;
}
