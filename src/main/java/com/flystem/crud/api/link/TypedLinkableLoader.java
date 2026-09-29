package com.flystem.crud.api.link;

import java.util.Collection;

/**
 * 单一目标类型的批量关联加载器：一次查出多个 id，避免对每条主数据单独查询。
 * 每实体一份生成产物（仅当该实体实现 {@link Linkable}）；由 {@link DefaultRelationshipLoader}
 * 按 {@link #supportType()} 分发。
 *
 * <p>返回 {@link LinkLoadResult}：本层 {@link LinkView}，以及按父 id 分组的下一级外键引用；
 * 是否继续加载由调用方按 {@link RelationRef#cascadePaths()} 决定。
 */
public interface TypedLinkableLoader {

    /** 本加载器负责的实体类型。 */
    Class<?> supportType();

    /**
     * 按主键批量加载。
     *
     * @param ids 主键集合；空则返回 {@link LinkLoadResult#empty()}
     * @return 视图 + 级联引用；未命中的 id 不出现在 {@code views} 中
     */
    LinkLoadResult loadByIds(Collection<Object> ids);
}
