package com.flystem.crud.api.link;

/**
 * 可作为关联目标的实体契约：对外只需暴露 {@link #getId()} 与 {@link #getLabel()}。
 * {@link com.flystem.crud.api.wrap.R} 的 {@code relationships} 侧经 {@link LinkView} 只序列化这两项。
 *
 * <p>被其它实体字段上的 {@link com.flystem.crud.api.annotation.Link} 引用的类型须实现本接口，
 * 否则生成期报错。
 */
public interface Linkable {

    /** 关联目标主键。 */
    Object getId();

    /** 展示用短标签（名称、标题等）。 */
    String getLabel();
}
