package com.flystem.crud.api.link;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 一条待解析的关联引用：由实现 {@link HasRelations} 的 DTO 从外键字段收集，供
 * {@link RelationshipLoader} 按目标类型批量加载。
 *
 * @param name         写入 {@code R.relationships} 的键
 * @param targetType   目标实体类型（须实现 {@link Linkable}）
 * @param id           外键值
 * @param cascadePaths 加载本目标后还要跟随的路径（点分关系名，相对本目标，不含本关联 {@code name}）；
 *                     空 = 不再级联。例：{@code @Link(name="author", cascade="company.manager")}
 *                     → {@code ["company.manager"]}
 */
public record RelationRef(String name, Class<?> targetType, Object id, List<String> cascadePaths) {

    public RelationRef {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(targetType, "targetType");
        Objects.requireNonNull(id, "id");
        cascadePaths = normalizePaths(cascadePaths);
    }

    /** 不级联。 */
    public static RelationRef of(String name, Class<?> targetType, Object id) {
        return of(name, targetType, id, List.of());
    }

    public static RelationRef of(String name, Class<?> targetType, Object id, List<String> cascadePaths) {
        return new RelationRef(name, targetType, id, cascadePaths);
    }

    public static RelationRef of(String name, Class<?> targetType, Object id, String... cascadePaths) {
        return of(name, targetType, id, cascadePaths == null ? List.of() : List.of(cascadePaths));
    }

    public RelationRef withCascadePaths(List<String> cascadePaths) {
        return new RelationRef(name, targetType, id, cascadePaths);
    }

    /** 是否还有下级路径要跟随。 */
    public boolean hasCascade() {
        return !cascadePaths.isEmpty();
    }

    private static List<String> normalizePaths(Collection<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String path : paths) {
            if (path == null) {
                continue;
            }
            String trimmed = path.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            out.add(trimmed);
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }
}
