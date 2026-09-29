package com.flystem.crud.api.meta;

import java.util.List;

/**
 * 实体的查询元信息（{@code CrudOperation.META} 的返回类型）：实体名 + 可查询字段说明 +
 * 关联关系说明（含级联路径）。
 *
 * <p>由生成的 ServiceImpl 按生成期已知的实体元数据直接构造，故它与实体定义<b>天然同步</b>——不存在
 * 「文档 / 前端字段表与实体漂移」的问题。端点为 {@code GET /{entity}/meta}（协议 §4.5）：不带查询条件
 * （它描述的是<b>实体</b>而非某个数据子集），也不分租户（各租户看到的字段集相同）。
 *
 * <p>不可变值对象，仅 getter（Jackson 可直接序列化为响应体）。{@code fields} /
 * {@code relationships} 以 {@link List#copyOf(java.util.Collection)} 快照。
 */
public final class QueryMeta {

    private final String entity;
    private final List<QueryFieldMeta> fields;
    private final List<QueryRelationMeta> relationships;

    private QueryMeta(
            String entity, List<QueryFieldMeta> fields, List<QueryRelationMeta> relationships) {
        this.entity = entity;
        this.fields = fields == null ? List.of() : List.copyOf(fields);
        this.relationships = relationships == null ? List.of() : List.copyOf(relationships);
    }

    /**
     * @param entity 实体资源名（{@code @Domain#name} 或实体简名，恒小写；与生成的
     *               {@code {P}{E}Meta#ENTITY} 同源）
     * @param fields 字段说明；{@code null} 视为空列表
     */
    public static QueryMeta of(String entity, List<QueryFieldMeta> fields) {
        return of(entity, fields, List.of());
    }

    /**
     * @param entity         实体资源名
     * @param fields         字段说明；{@code null} 视为空列表
     * @param relationships  关联说明（含级联）；{@code null} 视为空列表
     */
    public static QueryMeta of(
            String entity, List<QueryFieldMeta> fields, List<QueryRelationMeta> relationships) {
        return new QueryMeta(entity, fields, relationships);
    }

    /** 实体资源名（恒小写，与 {@code @Domain#name} / 生成的 {@code {P}{E}Meta#ENTITY} 同源）。 */
    public String getEntity() {
        return entity;
    }

    /** 字段说明列表（不可变，永不为 {@code null}；含租户字段，由 {@code tenantField} 标记）。 */
    public List<QueryFieldMeta> getFields() {
        return fields;
    }

    /**
     * 关联说明列表（不可变，永不为 {@code null}）：{@code @Link} 声明的关系名、外键字段、目标类型、
     * 级联路径；与读侧 {@code R.relationships} 键及默认级联一致。
     */
    public List<QueryRelationMeta> getRelationships() {
        return relationships;
    }

    @Override
    public String toString() {
        return "QueryMeta{entity=" + entity + ", fields=" + fields.size()
                + ", relationships=" + relationships.size() + "}";
    }
}
