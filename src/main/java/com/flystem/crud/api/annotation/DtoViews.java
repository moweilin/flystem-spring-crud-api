package com.flystem.crud.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体字段在各类 DTO 中的可见性：系统用户面 {@code Item}/{@code Detail}、管理面
 * {@code AdminItem}/{@code AdminDetail}、第三方 Guest 面 {@code GuestItem}/{@code GuestDetail}。
 *
 * <p>可标在<strong>字段</strong>或<strong>属性访问器</strong>（getter / {@code isXxx}）上；同一属性两者
 * 皆有时，<b>方法上的注解优先于字段</b>（便于 Lombok / 手写 getter 就地收紧列表项暴露面）。
 *
 * <p>未标注时的默认：普通持久化字段各类皆 true；租户字段（约定名 {@code tenantId}）仅管理面
 * true；guest 字段（约定名 {@code guestId}）管理面与 Guest 面 true。显式书写本注解即覆盖该默认。
 *
 * <p>主键（{@code id} / {@code @TableId}）始终进入全部已生成面，不受本注解约束。
 *
 * @see DtoGeneration
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface DtoViews {

    /** 是否进入系统用户面列表项 DTO（{@code {P}{E}ItemDTO}）。 */
    boolean item() default true;

    /** 是否进入系统用户面详情 DTO（{@code {P}{E}DetailDTO}）。 */
    boolean detail() default true;

    /** 是否进入管理面列表项 DTO（{@code {P}{E}AdminItemDTO}）。 */
    boolean adminItem() default true;

    /** 是否进入管理面详情 DTO（{@code {P}{E}AdminDetailDTO}）。 */
    boolean adminDetail() default true;

    /** 是否进入 Guest 面列表项 DTO（{@code {P}{E}GuestItemDTO}）。 */
    boolean guestItem() default true;

    /** 是否进入 Guest 面详情 DTO（{@code {P}{E}GuestDetailDTO}）。 */
    boolean guestDetail() default true;
}
