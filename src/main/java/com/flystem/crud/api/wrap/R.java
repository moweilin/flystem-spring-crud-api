package com.flystem.crud.api.wrap;

import com.flystem.crud.api.link.LinkView;
import com.flystem.crud.api.link.RelationshipLoader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 本库<b>默认</b>的统一结果封装（ADR 0020）：{@code code} + {@code message} + {@code data}，
 * 另含 JSON:API 兼容的独立 {@code relationships}（关联名 → 仅 id/label 的 {@link LinkView} 列表）。
 * 不可变值对象、仅 getter，Jackson 可直接序列化为响应体。
 *
 * <p>由 {@link ResultWrapper#wrap} 产出——配 {@code @ControllerGeneration.resultWrapper} 为
 * {@link DefaultResultWrapper}（或任何实现 {@link ResultWrapper} 的类）时，生成端点返回
 * {@code R<数据类型>}。本类本身<b>不是</b>合法的 {@code resultWrapper} 配置值（未实现 SPI）；要换信封
 * 形状，声明自己的 {@link ResultWrapper} bean 在 {@code wrap} 里组装即可。
 *
 * <p>{@code relationships} 由 {@link DefaultResultWrapper} 在包装时经 {@link RelationshipLoader}
 * 批量填充；无关联或未启用加载器时为空 Map。
 *
 * @param <T> 被包装的数据类型
 */
public class R<T> {

    /** 成功码。 */
    public static final int CODE_OK = 0;

    /** 通用失败码——自定义 {@link ResultWrapper} 兜异常时的缺省取值，使用方可改用自己的码表。 */
    public static final int CODE_ERROR = -1;

    private final int code;

    private final String message;

    private final T data;

    private final Map<String, List<LinkView>> relationships;

    /** 成功结果（无关联）。 */
    public R(T data) {
        this(CODE_OK, null, data, Map.of());
    }

    /** 成功结果（带关联）。 */
    public R(T data, Map<String, List<LinkView>> relationships) {
        this(CODE_OK, null, data, relationships);
    }

    public R(int code, String message, T data) {
        this(code, message, data, Map.of());
    }

    public R(int code, String message, T data, Map<String, List<LinkView>> relationships) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.relationships = relationships == null || relationships.isEmpty()
                ? Map.of()
                : copyRelationships(relationships);
    }

    /** 成功结果（无关联）。 */
    public static <T> R<T> ok(T data) {
        return new R<>(data);
    }

    /** 成功结果（带关联；{@code relationships} 为 null/空 → 空 Map）。 */
    public static <T> R<T> ok(T data, Map<String, List<LinkView>> relationships) {
        return new R<>(data, relationships);
    }

    /** 失败结果（无数据、无关联）。 */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null, Map.of());
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /** 真正的返回数据。 */
    public T getData() {
        return data;
    }

    /**
     * 本响应涉及的全部关联资源（按关系名分组、组内按 id 去重）。永不为 {@code null}；无关联时为空
     * Map。形状对齐 JSON:API「关联独立存放」——不与 {@code data} 内属性混排。
     */
    public Map<String, List<LinkView>> getRelationships() {
        return relationships;
    }

    /** 是否成功（{@code code == } {@link #CODE_OK}）。 */
    public boolean isSuccess() {
        return code == CODE_OK;
    }

    private static Map<String, List<LinkView>> copyRelationships(Map<String, List<LinkView>> source) {
        Map<String, List<LinkView>> copy = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, List<LinkView>> e : source.entrySet()) {
            if (e.getKey() == null || e.getValue() == null || e.getValue().isEmpty()) {
                continue;
            }
            copy.put(e.getKey(), List.copyOf(e.getValue()));
        }
        return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
    }

    @Override
    public String toString() {
        return "R{code=" + code + ", message=" + message + ", data=" + data
                + ", relationships=" + relationships.keySet() + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof R<?> that)) {
            return false;
        }
        return code == that.code
                && Objects.equals(message, that.message)
                && Objects.equals(data, that.data)
                && Objects.equals(relationships, that.relationships);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, data, relationships);
    }
}
