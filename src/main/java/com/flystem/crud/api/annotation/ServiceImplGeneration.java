package com.flystem.crud.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Service 实现产物层的独立配置：Service 实现（类名 = 前缀 + 实体名 + {@code ServiceImpl}），
 * 实现同实体的 Service 接口。
 *
 * <p>仅作为 {@link CrudGeneration}（包级默认）/ {@link Domain}（实体级覆盖）配置属性的
 * 嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ServiceImplGeneration {

    /** 是否生成本层（裁剪开关）：默认生成。 */
    boolean enabled() default true;

    /** 是否归入 api 产物：Service 实现默认归 impl 侧。 */
    boolean api() default false;
}
