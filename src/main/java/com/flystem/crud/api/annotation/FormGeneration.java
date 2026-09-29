package com.flystem.crud.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Form 产物层的独立配置：实体的 web 输入表单对象（类名 = 前缀 + 实体名 + {@code Form}，
 * 如 {@code FUserForm}），Controller 的入参载体；与 DTO 的职责边界及是否按用途拆分
 * 待产物细节工作包确定。
 *
 * <p>仅作为 {@link CrudGeneration}（包级默认）/ {@link Domain}（实体级覆盖）配置属性的
 * 嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface FormGeneration {

    /** 是否生成本层（裁剪开关）：默认生成。 */
    boolean enabled() default true;

    /** 是否归入 api 产物：Form 默认归 impl 侧（随其主要消费方 Controller 同侧）。 */
    boolean api() default false;
}
