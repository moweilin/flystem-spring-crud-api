package com.flystem.crud.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MapStruct 转换产物层的独立配置：实体 ↔ DTO 转换器（MapStruct {@code @Mapper} 接口，
 * 类名 = 前缀 + 实体名 + {@code Converter}，如 {@code FUserConverter}——避免与数据访问层
 * 的 Mapper 撞名）。
 *
 * <p>默认关闭（opt-in）：生成本层要求使用方引入 mapstruct 依赖与注解处理器，默认打开会
 * 给未准备该依赖的项目强加第三方依赖（违背 ADR 0008 非侵入承诺）。
 *
 * <p>仅作为 {@link CrudGeneration}（包级默认）/ {@link Domain}（实体级覆盖）配置属性的
 * 嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface MapStructGeneration {

    /** 是否生成本层（裁剪开关）：默认不生成——opt-in，需使用方引入 mapstruct 依赖与注解处理器。 */
    boolean enabled() default false;

    /** 是否归入 api 产物：转换层默认归 impl 侧。 */
    boolean api() default false;
}
