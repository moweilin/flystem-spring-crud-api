package com.flystem.crud.api.link;

import com.flystem.crud.api.page.PageResult;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 从业务出参收集 {@link RelationRef} 并<b>按目标类型批量</b>解析为
 * {@code R.relationships}（关系名 → 去重后的 {@link LinkView} 列表）；默认实现沿实体
 * {@code @Link} <b>级联</b>（BFS，防环）。
 *
 * <p>默认实现 {@link DefaultRelationshipLoader} 聚合所有 {@link TypedLinkableLoader} bean。
 */
public interface RelationshipLoader {

    /**
     * 解析引用集合。
     *
     * @param refs 待解析引用；空则返回空 Map
     * @return 关系名 → LinkView 列表（同名下去重、保首次出现序）；永不为 {@code null}
     */
    Map<String, List<LinkView>> resolve(Collection<RelationRef> refs);

    /**
     * 从任意出参收集引用：识别 {@link HasRelations}、{@link PageResult} 的 items、以及
     * {@link Iterable} 元素。
     */
    static List<RelationRef> collectRefs(Object data) {
        return RelationRefCollector.collect(data);
    }
}
