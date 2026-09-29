package com.flystem.crud.api.enums;

/**
 * 生成范围：api / impl 两侧产物可独立生成，也可同时生成（CONTEXT.md「api 产物」「impl 产物」）。
 *
 * <p>典型用法：api 模块 {@link #API_ONLY} 先行生成并发布接口侧，impl 模块 {@link #IMPL_ONLY}
 * 依赖 api jar 只生成实现侧。
 */
public enum GenerationMode {

    /** api 与 impl 同时生成（默认）。 */
    BOTH,

    /** 仅生成 api 产物（各层是否归 api 由层配置的 {@code api} 属性声明）。 */
    API_ONLY,

    /** 仅生成 impl 产物（要求 api 产物已存在：此前生成过，或来自依赖 jar）。 */
    IMPL_ONLY
}
