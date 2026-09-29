package com.flystem.crud.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据访问产物层的独立配置：按实体识别路径二选一产出——JPA 实体生成 Repository（继承
 * Spring Data {@code JpaRepository}），MyBatis 实体生成 Mapper（形态由识别注解决定的生态
 * 分叉，ADR 0011）。
 *
 * <p>本层产物恒为接口：实现由 Spring Data / MyBatis 在运行期代理产生，不在源码产物中，
 * 故本层无 Service 层那样的 Impl 配置层。
 *
 * <p>仅作为 {@link CrudGeneration}（包级默认）/ {@link Domain}（实体级覆盖）配置属性的
 * 嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface DataAccessGeneration {

    /** 是否生成本层（裁剪开关）：默认生成。 */
    boolean enabled() default true;

    /** 是否归入 api 产物：数据访问层默认归 impl 侧。 */
    boolean api() default false;
}
