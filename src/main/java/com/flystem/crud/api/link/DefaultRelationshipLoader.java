package com.flystem.crud.api.link;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 默认 {@link RelationshipLoader}：按类型批量加载；按 {@link RelationRef#cascadePaths()} 中的
 * 点分关系名路径 BFS 级联（空路径 = 只加载当前目标）。同一 {@code (targetType, id)} 只加载一次；
 * 另有绝对深度上限 {@link #MAX_DEPTH}。
 */
public final class DefaultRelationshipLoader implements RelationshipLoader {

    /** 绝对最大跳数（含首层），防止路径配置异常。 */
    public static final int MAX_DEPTH = 16;

    private final Map<Class<?>, TypedLinkableLoader> loadersByType;

    public DefaultRelationshipLoader(Collection<? extends TypedLinkableLoader> loaders) {
        Map<Class<?>, TypedLinkableLoader> map = new HashMap<>();
        if (loaders != null) {
            for (TypedLinkableLoader loader : loaders) {
                if (loader == null || loader.supportType() == null) {
                    continue;
                }
                TypedLinkableLoader prev = map.put(loader.supportType(), loader);
                if (prev != null && prev != loader) {
                    throw new IllegalStateException("duplicate TypedLinkableLoader for "
                            + loader.supportType().getName() + ": " + prev.getClass().getName()
                            + " and " + loader.getClass().getName());
                }
            }
        }
        this.loadersByType = Map.copyOf(map);
    }

    @Override
    public Map<String, List<LinkView>> resolve(Collection<RelationRef> refs) {
        if (refs == null || refs.isEmpty()) {
            return Map.of();
        }
        Map<String, List<LinkView>> relationships = new LinkedHashMap<>();
        Map<String, Set<Object>> seenIdsByName = new HashMap<>();
        Map<String, LinkView> viewCache = new HashMap<>();
        Set<String> fetchedKeys = new HashSet<>();

        List<RelationRef> frontier = mergeFrontier(refs);
        int depth = 0;
        while (!frontier.isEmpty() && depth < MAX_DEPTH) {
            Map<Class<?>, Set<Object>> toFetch = new LinkedHashMap<>();
            for (RelationRef ref : frontier) {
                String key = loadKey(ref.targetType(), ref.id());
                if (viewCache.containsKey(key) || !fetchedKeys.add(key)) {
                    continue;
                }
                toFetch.computeIfAbsent(ref.targetType(), t -> new LinkedHashSet<>()).add(ref.id());
            }

            Map<Class<?>, LinkLoadResult> batches = new HashMap<>();
            for (Map.Entry<Class<?>, Set<Object>> e : toFetch.entrySet()) {
                TypedLinkableLoader loader = loadersByType.get(e.getKey());
                if (loader == null || e.getValue().isEmpty()) {
                    continue;
                }
                LinkLoadResult batch = loader.loadByIds(e.getValue());
                if (batch == null || batch.isEmpty()) {
                    continue;
                }
                batches.put(e.getKey(), batch);
                for (Map.Entry<Object, LinkView> ve : batch.views().entrySet()) {
                    if (ve.getKey() == null || ve.getValue() == null) {
                        continue;
                    }
                    viewCache.put(loadKey(e.getKey(), ve.getKey()), ve.getValue());
                }
            }

            for (RelationRef ref : frontier) {
                LinkView view = viewCache.get(loadKey(ref.targetType(), ref.id()));
                if (view == null) {
                    continue;
                }
                Set<Object> seen = seenIdsByName.computeIfAbsent(ref.name(), n -> new LinkedHashSet<>());
                if (!seen.add(view.getId())) {
                    continue;
                }
                relationships.computeIfAbsent(ref.name(), n -> new ArrayList<>()).add(view);
            }

            List<RelationRef> next = new ArrayList<>();
            for (RelationRef ref : frontier) {
                if (!ref.hasCascade()) {
                    continue;
                }
                LinkLoadResult batch = batches.get(ref.targetType());
                if (batch == null) {
                    continue;
                }
                List<RelationRef> outs = batch.cascadesByParentId().get(ref.id());
                if (outs == null || outs.isEmpty()) {
                    continue;
                }
                for (RelationRef out : outs) {
                    List<String> nextPaths = matchNextPaths(ref.cascadePaths(), out.name());
                    if (nextPaths == null) {
                        continue; // 无路径命中该关系名
                    }
                    next.add(out.withCascadePaths(nextPaths));
                }
            }
            frontier = mergeFrontier(next);
            depth++;
        }

        if (relationships.isEmpty()) {
            return Map.of();
        }
        Map<String, List<LinkView>> out = new LinkedHashMap<>();
        for (Map.Entry<String, List<LinkView>> e : relationships.entrySet()) {
            out.put(e.getKey(), List.copyOf(e.getValue()));
        }
        return Map.copyOf(out);
    }

    /**
     * 从剩余路径中匹配下一段关系名 {@code segment}。
     *
     * @return {@code null} 未命中；空列表 = 命中且本路径到此结束（加载该目标、不再往下）；
     *         非空 = 命中且还有后缀要继续跟
     */
    static List<String> matchNextPaths(List<String> cascadePaths, String segment) {
        if (cascadePaths == null || cascadePaths.isEmpty() || segment == null || segment.isBlank()) {
            return null;
        }
        List<String> next = new ArrayList<>();
        boolean matched = false;
        for (String path : cascadePaths) {
            if (path.equals(segment)) {
                matched = true;
                continue;
            }
            String prefix = segment + ".";
            if (path.startsWith(prefix)) {
                matched = true;
                String rest = path.substring(prefix.length());
                if (!rest.isBlank()) {
                    next.add(rest);
                }
            }
        }
        if (!matched) {
            return null;
        }
        return next.isEmpty() ? List.of() : List.copyOf(next);
    }

    /** 同一 {@code (name, type, id)} 合并 cascade 路径（并集）。 */
    private static List<RelationRef> mergeFrontier(Collection<RelationRef> refs) {
        Map<String, RelationRef> best = new LinkedHashMap<>();
        for (RelationRef ref : refs) {
            if (ref == null) {
                continue;
            }
            String edge = ref.name() + "|" + loadKey(ref.targetType(), ref.id());
            RelationRef prev = best.get(edge);
            if (prev == null) {
                best.put(edge, ref);
                continue;
            }
            Set<String> paths = new LinkedHashSet<>(prev.cascadePaths());
            paths.addAll(ref.cascadePaths());
            best.put(edge, prev.withCascadePaths(List.copyOf(paths)));
        }
        return new ArrayList<>(best.values());
    }

    private static String loadKey(Class<?> type, Object id) {
        return type.getName() + "#" + id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DefaultRelationshipLoader that
                && Objects.equals(loadersByType.keySet(), that.loadersByType.keySet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(loadersByType.keySet());
    }
}
