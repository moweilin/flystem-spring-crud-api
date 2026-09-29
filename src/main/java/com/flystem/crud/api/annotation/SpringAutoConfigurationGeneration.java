package com.flystem.crud.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Spring 自动配置产物层的独立配置：实体的自动配置类（类名 = 前缀 + 实体名 +
 * {@code AutoConfiguration}，如 {@code FUserAutoConfiguration}），承担该实体生成产物
 * 在 Spring 上下文的装配（工厂、Service 实现等 Bean 注册，ADR 0004 的运行期侧面）。
 *
 * <p>生成粒度（每实体一份 vs 每生成单元一份）与注册机制（Boot 3/4 的
 * {@code AutoConfiguration.imports} 资源文件）待运行期支撑工作包确定。
 *
 * <p>仅作为 {@link CrudGeneration}（包级默认）/ {@link Domain}（实体级覆盖）配置属性的
 * 嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface SpringAutoConfigurationGeneration {

    /** 是否生成本层（裁剪开关）：默认生成。 */
    boolean enabled() default true;

    /**
     * 是否归入 api 产物：默认归 impl 侧——装配引用 impl 侧 Bean（Service 实现、工厂装配），
     * 而 api 不得依赖 impl，归 impl 为编译安全侧。
     */
    boolean api() default false;
}
