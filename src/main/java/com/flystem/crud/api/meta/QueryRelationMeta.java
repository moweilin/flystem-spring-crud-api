package com.flystem.crud.api.meta;

import java.util.List;

/**
 * 单个关联关系的说明（{@link QueryMeta#getRelationships()} 的元素）。
 *
 * <p>对应实体外键上的 {@code @Link}：关系名、外键字段、目标类型简名、相对本目标的级联路径
 * （不含本关联 {@code name}）。供前端 / MCP 获知读侧 {@code R.relationships} 会暴露哪些键、
 * 以及默认会跟到哪几段级联。
 *
 * <p>不可变值对象，仅 getter（Jackson 可直接序列化为响应体）。{@code cascade} 以
 * {@link List#copyOf(java.util.Collection)} 快照。
 */
public final class QueryRelationMeta {

    private final String name;
    private final String field;
    private final String target;
    private final List<String> cascade;

    private QueryRelationMeta(String name, String field, String target, List<String> cascade) {
        this.name = name;
        this.field = field;
        this.target = target;
        this.cascade = cascade == null ? List.of() : List.copyOf(cascade);
    }

    /**
     * @param name    {@code @Link#name}：写入 {@code R.relationships} 的键
     * @param field   外键实体属性名（如 {@code authorId}）
     * @param target  关联目标 Java 类型简名（如 {@code Author}）
     * @param cascade 相对本目标的级联路径（点分关系名，不含本关联 {@code name}）；{@code null} 视为空
     */
    public static QueryRelationMeta of(String name, String field, String target, List<String> cascade) {
        return new QueryRelationMeta(name, field, target, cascade);
    }

    /** 无级联时的便捷工厂（等价于 {@code cascade = List.of()}）。 */
    public static QueryRelationMeta of(String name, String field, String target) {
        return of(name, field, target, List.of());
    }

    /** {@code @Link#name}。 */
    public String getName() {
        return name;
    }

    /** 外键实体属性名。 */
    public String getField() {
        return field;
    }

    /** 关联目标 Java 类型简名。 */
    public String getTarget() {
        return target;
    }

    /** 相对本目标的级联路径（不可变，永不为 {@code null}）。 */
    public List<String> getCascade() {
        return cascade;
    }

    @Override
    public String toString() {
        return "QueryRelationMeta{name=" + name + ", field=" + field + ", target=" + target
                + ", cascade=" + cascade + "}";
    }
}
