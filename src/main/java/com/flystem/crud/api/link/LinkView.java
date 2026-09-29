package com.flystem.crud.api.link;

import java.util.Objects;

/**
 * 关联资源的精简视图（JSON:API 兼容的 id + 展示标签）：写入 {@link com.flystem.crud.api.wrap.R}
 * 的 {@code relationships}，刻意不含其它属性。
 */
public final class LinkView implements Linkable {

    private final Object id;
    private final String label;

    public LinkView(Object id, String label) {
        this.id = Objects.requireNonNull(id, "id");
        this.label = label;
    }

    /** 从任意 {@link Linkable} 截取 id/label（丢弃实体其它字段）。 */
    public static LinkView of(Linkable source) {
        Objects.requireNonNull(source, "source");
        return new LinkView(source.getId(), source.getLabel());
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LinkView that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label);
    }

    @Override
    public String toString() {
        return "LinkView{id=" + id + ", label=" + label + "}";
    }
}
