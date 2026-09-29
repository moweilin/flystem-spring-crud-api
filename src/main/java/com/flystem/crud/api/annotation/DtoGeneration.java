package com.flystem.crud.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DTO 产物层的独立配置：数据传输对象——系统用户面 Item/Detail、管理面 AdminItem/AdminDetail、
 * Guest 面 GuestItem/GuestDetail（后两类仅在对应层开启时生成；类名 = 前缀 + 实体名 +
 *（{@code Admin}|{@code Guest}）+ {@code Item}|{@code Detail} + {@code DTO}）。字段级可见性见
 * {@link DtoViews}。
 *
 * <p>仅作为 {@link CrudGeneration}（包级默认）/ {@link Domain}（实体级覆盖）配置属性的
 * 嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface DtoGeneration {

    /** 是否生成本层（裁剪开关）：默认生成。 */
    boolean enabled() default true;

    /**
     * 是否归入 api 产物：DTO 默认归 api 侧。注意：api 侧签名引用的 DTO 必归 api——
     * 编译约束，优先于本配置（配为 {@code false} 时被引用的 DTO 仍归 api）。
     */
    boolean api() default true;
}
