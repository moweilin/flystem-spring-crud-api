package com.flystem.crud.api.annotation;

import com.flystem.crud.api.enums.TaskScope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Service 接口产物层的独立配置：实体 Service 接口（类名 = 前缀 + 实体名 + {@code Service}）。
 * ServiceFactory 归并于本层（ADR 0004）：随 Service 接口一并生成，共用本层的裁剪开关与
 * api/impl 归属，无独立层配置——工厂与其引用的 Service 接口恒同侧，api 侧编译约束天然满足。
 *
 * <p>仅作为 {@link CrudGeneration}（包级默认）/ {@link Domain}（实体级覆盖）配置属性的
 * 嵌套值使用，不可直接标注（{@code @Target({})}）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ServiceGeneration {

    /** 是否生成本层（裁剪开关）：默认生成；关闭则 Service 接口与 ServiceFactory 均不生成。 */
    boolean enabled() default true;

    /** 是否归入 api 产物：Service 接口与随其生成的 ServiceFactory 默认归 api 侧。 */
    boolean api() default true;

    /**
     * 异步导入 / 导出任务的可见性范围粒度（ADR 0022）：决定工厂方法 {@code taskScope(user)}
     * 产出用户级还是租户级非空字符串。默认 {@link TaskScope#USER}；改为 {@link TaskScope#TENANT}
     * 仅多租户实体合法（单租户配了即生成报错）。
     */
    TaskScope taskScope() default TaskScope.USER;
}
