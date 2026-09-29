package com.flystem.crud.api.link;

import java.util.List;

/**
 * 出参 DTO 声明自身携带的关联外键引用。生成的 Item/Detail（及 Admin*）DTO 在存在
 * {@code @Link} 字段时实现本接口；{@link com.flystem.crud.api.wrap.DefaultResultWrapper}
 * 在 {@code wrap} 时收集这些引用并批量解析进 {@code R.relationships}。
 */
public interface HasRelations {

    /**
     * 本对象上的关联引用；外键为 {@code null} 的项不得列入。返回列表可为不可变空表，不得为
     * {@code null}。
     */
    List<RelationRef> relationRefs();
}
