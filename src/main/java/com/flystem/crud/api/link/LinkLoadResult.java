package com.flystem.crud.api.link;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 一次按类型批量加载的结果：本层 {@link LinkView}，以及按父实体 id 分组的下一级外键引用
 * （供 {@link DefaultRelationshipLoader} 按 {@link RelationRef#cascadePaths()} 继续解析）。
 *
 * @param views              id → 视图；永不为 {@code null}
 * @param cascadesByParentId 父实体主键 → 该行上 {@code @Link} 抽出的引用（路径由调用方填充）；永不为 null
 */
public record LinkLoadResult(
        Map<Object, LinkView> views, Map<Object, List<RelationRef>> cascadesByParentId) {

    public LinkLoadResult {
        views = views == null || views.isEmpty() ? Map.of() : Map.copyOf(views);
        cascadesByParentId = copyCascades(cascadesByParentId);
    }

    public static LinkLoadResult empty() {
        return new LinkLoadResult(Map.of(), Map.of());
    }

    public static LinkLoadResult of(Map<Object, LinkView> views, Map<Object, List<RelationRef>> cascadesByParentId) {
        return new LinkLoadResult(views, cascadesByParentId);
    }

    /** 无级联出口时的便捷工厂。 */
    public static LinkLoadResult viewsOnly(Map<Object, LinkView> views) {
        return new LinkLoadResult(views, Map.of());
    }

    public boolean isEmpty() {
        return views.isEmpty();
    }

    private static Map<Object, List<RelationRef>> copyCascades(Map<Object, List<RelationRef>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<Object, List<RelationRef>> copy = new LinkedHashMap<>();
        for (Map.Entry<Object, List<RelationRef>> e : source.entrySet()) {
            if (e.getKey() == null || e.getValue() == null || e.getValue().isEmpty()) {
                continue;
            }
            copy.put(e.getKey(), List.copyOf(e.getValue()));
        }
        return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LinkLoadResult that)) {
            return false;
        }
        return Objects.equals(views, that.views) && Objects.equals(cascadesByParentId, that.cascadesByParentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(views, cascadesByParentId);
    }

    /** 可变构建器，供生成的 loader 填充。 */
    public static final class Builder {
        private final Map<Object, LinkView> views = new LinkedHashMap<>();
        private final Map<Object, List<RelationRef>> cascades = new LinkedHashMap<>();

        public Builder view(Object id, LinkView view) {
            if (id != null && view != null) {
                views.put(id, view);
            }
            return this;
        }

        public Builder cascade(Object parentId, RelationRef ref) {
            if (parentId != null && ref != null) {
                cascades.computeIfAbsent(parentId, k -> new ArrayList<>()).add(ref);
            }
            return this;
        }

        public LinkLoadResult build() {
            return LinkLoadResult.of(views, cascades);
        }
    }
}
