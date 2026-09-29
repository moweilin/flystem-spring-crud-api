package com.flystem.crud.api.annotation;

import com.flystem.crud.api.link.Linkable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将实体上的外键字段声明为关联：目标类型须实现 {@link Linkable}。读侧 DTO 仍保留该外键标量；
 * 启用结果包装时，{@link com.flystem.crud.api.wrap.DefaultResultWrapper} 在 {@code wrap} 中按类型
 * 批量加载目标，写入 {@link com.flystem.crud.api.wrap.R} 的独立 {@code relationships} 字段
 * （JSON:API 风格的旁路关联集，值为仅含 id/label 的 {@link com.flystem.crud.api.link.LinkView}）。
 *
 * <p>可标在字段或 getter 上；同一属性两者皆有时，<b>方法优先于字段</b>（同 {@link DtoViews}）。
 *
 * <p>{@link #name()} <b>必填</b>：写入 {@code R.relationships} 的键，也是下游 {@code @Link#name}
 * 级联匹配所用的关系名。
 *
 * <p>{@link #cascade()} 为相对本目标的点分路径（<b>不含</b>本关联 {@link #name()}）；空（默认）=
 * 只加载本关联。例：本关联 {@code name = "author"} 时写 {@code cascade = "company.manager"}，
 * 表示加载 author 后，再跟目标实体上名为 {@code company}、{@code manager} 的关联。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Link {

    /** 关联目标实体类型（须实现 {@link Linkable}）。 */
    Class<? extends Linkable> value();

    /**
     * 写入 {@code R.relationships} 的键（必填，非空）；下游级联路径中的关系名须与此一致。
     */
    String name();

    /**
     * 级联目标路径（点分关系名，相对本关联目标，<b>不含</b>本关联 {@link #name()}）。
     * 空数组（默认）= 只加载本目标。例：{@code {"company.manager"}}。
     */
    String[] cascade() default {};
}
