package com.flystem.crud.api.meta;

/**
 * 单个可查询字段的说明（{@link QueryMeta} 的元素，{@code CrudOperation.META} 的产出）。
 *
 * <p>用途：前端据此动态渲染查询表单、MCP 工具据此描述参数——不必把实体类或查询条件类的字节码
 * 暴露给消费方。字段集来自生成期的实体元数据，故与实体定义天然同步，不会漂移。
 *
 * <p>不可变值对象，仅 getter（Jackson 可直接序列化为响应体）。<b>当前不含</b>「含义 / 显示名」与
 * 「可用操作符」两项：前者要等实体侧有标注来源（如字段级注解），后者要等查询条件类的字段条件落地
 * （协议 §8 登记为 wb#6）——届时按操作符与字段类型配对给出（如数值型给 {@code gt/lt/between}、
 * 字符串型给 {@code like}）。增补属性是非破坏变更。
 */
public final class QueryFieldMeta {

    private final String name;
    private final String type;
    private final String column;
    private final boolean tenantField;

    private QueryFieldMeta(String name, String type, String column, boolean tenantField) {
        this.name = name;
        this.type = type;
        this.column = column;
        this.tenantField = tenantField;
    }

    /**
     * @param name        实体属性名（查询条件类里将用的字段名）
     * @param type        Java 类型<b>简名</b>（如 {@code String} / {@code UUID} / {@code Long}）——给消费方
     *                    看的是名字而非 {@code Class}，避免元信息把类型引用带进响应契约
     * @param column      数据库列名（诊断与排序白名单用）
     * @param tenantField 是否租户字段：为 {@code true} 时该字段由租户边界自动约束（ADR 0013），
     *                    <b>不应</b>作为用户可填的查询条件暴露，故显式标出供前端排除
     */
    public static QueryFieldMeta of(String name, String type, String column, boolean tenantField) {
        return new QueryFieldMeta(name, type, column, tenantField);
    }

    /** 实体属性名。 */
    public String getName() {
        return name;
    }

    /** Java 类型简名。 */
    public String getType() {
        return type;
    }

    /** 数据库列名。 */
    public String getColumn() {
        return column;
    }

    /** 是否租户字段（是则不应作为可填查询条件暴露）。 */
    public boolean isTenantField() {
        return tenantField;
    }

    @Override
    public String toString() {
        return "QueryFieldMeta{name=" + name + ", type=" + type + ", column=" + column
                + ", tenantField=" + tenantField + "}";
    }
}
