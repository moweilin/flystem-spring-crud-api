package com.flystem.crud.api.page;

import java.util.List;
import java.util.Objects;

/**
 * 中立分页结果（ADR 0019）：承载「一页数据 + 分页元数据」，不绑定任何持久层或 Web 框架的分页类型。
 * 生成的分页产物（assembler 的 {@code toDtoPage}，以及 wb#6 的 Service PAGE 方法与 Controller 分页
 * 端点）一律以本类型出入，故产物的对外契约不泄漏 MyBatis-Plus 的 {@code IPage} 或 Spring Data 的
 * {@code Page}——换持久层只改产物内部的适配代码，不改调用方签名。
 *
 * <p><b>页码从 1 开始</b>（{@link #getCurrentPage()}），与 MyBatis-Plus 一致；Spring Data 的
 * {@code Page.getNumber()} 从 0 开始，适配时须 {@code +1}。构造时对 {@code currentPage < 1} 直接抛
 * {@link IllegalArgumentException}——这是两套分页语义对接时最易踩的错位，把它暴露在写入点，
 * 而不是留成静默的错误数据（同 ADR 0005「不静默兜底」的取向）。
 *
 * <p>{@code pageSize <= 0} 表示「未分页」（MyBatis-Plus 以 {@code size < 0} 表达取全量），此时
 * {@link #getPages()} 返回 0：无分页语义时总页数无定义，不猜一个值。
 *
 * <p>不可变值对象：{@code items} 以 {@link List#copyOf(java.util.Collection)} 快照，故为不可变
 * 列表，外部改动原列表不影响本对象（{@code items} 传 {@code null} 视为空页；元素不得为
 * {@code null}，这是 {@code List.copyOf} 的语义）。仅提供 getter，Jackson 可直接序列化为响应体
 * （{@code items} / {@code currentPage} / {@code pageSize} / {@code total} / {@code pages}）；<b>不设</b>
 * 无参构造与 setter，故不用于反序列化——本类型是<b>出参</b>契约；入参侧的对应类型是 {@link PageParam}。
 *
 * <p>页内元素叫 {@code items} 而<b>不叫</b> {@code records}：后者是 MyBatis-Plus {@code IPage.getRecords()}
 * 的词汇，本类型的存在理由正是不泄漏持久层词汇（换 Spring Data 时那边叫 {@code getContent()}，两个都不中立）。
 * {@code items} 与具体生态无关，也与 {@code total}（总条数）读起来成对。
 *
 * <p><b>字段名与 {@link PageParam} 对齐</b>（{@code currentPage} / {@code pageSize}），使同一次分页往返的
 * 请求体与响应体用同一套词汇——此前本类沿用 MyBatis-Plus 的 {@code current} / {@code size}，与入参侧
 * 的 {@code currentPage} / {@code pageSize} 不同形，读写两侧对不上。对齐方向取入参侧：{@code currentPage}
 * 比 {@code current} 明确（不与「当前记录」歧义），且改名代价此刻最低（见 ADR 0019 的演进段）。
 *
 * @param <T> 页内元素类型（产物侧为 DTO）
 */
public final class PageResult<T> {

    private final List<T> items;
    private final long currentPage;
    private final long pageSize;
    private final long total;

    private PageResult(List<T> items, long currentPage, long pageSize, long total) {
        if (currentPage < 1) {
            throw new IllegalArgumentException("currentPage must be >= 1（页码从 1 开始；Spring Data 的 "
                    + "getNumber() 需 +1）, but was " + currentPage);
        }
        if (total < 0) {
            throw new IllegalArgumentException("total must be >= 0, but was " + total);
        }
        this.items = items == null ? List.of() : List.copyOf(items);
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.total = total;
    }

    /**
     * 构造一页结果。
     *
     * @param items       页内元素；{@code null} 视为空页
     * @param currentPage 当前页码，从 1 开始
     * @param pageSize    每页条数；{@code <= 0} 表示未分页
     * @param total       总记录数
     * @throws IllegalArgumentException {@code currentPage < 1} 或 {@code total < 0}
     */
    public static <T> PageResult<T> of(List<T> items, long currentPage, long pageSize, long total) {
        return new PageResult<>(items, currentPage, pageSize, total);
    }

    /**
     * 构造空页（无记录、总数 0）：查询无命中时使用，让调用方不必处理 {@code null}。
     *
     * @param currentPage 当前页码，从 1 开始
     * @param pageSize    每页条数；{@code <= 0} 表示未分页
     * @throws IllegalArgumentException {@code currentPage < 1}
     */
    public static <T> PageResult<T> empty(long currentPage, long pageSize) {
        return new PageResult<>(List.of(), currentPage, pageSize, 0L);
    }

    /**
     * 页内元素：不可变列表，永不为 {@code null}。
     */
    public List<T> getItems() {
        return items;
    }

    /**
     * 当前页码，从 1 开始。
     */
    public long getCurrentPage() {
        return currentPage;
    }

    /**
     * 每页条数；{@code <= 0} 表示未分页。
     */
    public long getPageSize() {
        return pageSize;
    }

    /**
     * 总记录数。
     */
    public long getTotal() {
        return total;
    }

    /**
     * 总页数 = {@code ceil(total / pageSize)}。未分页（{@code pageSize <= 0}）或无记录（{@code total <= 0}）
     * 时返回 0。
     */
    public long getPages() {
        if (pageSize <= 0 || total <= 0) {
            return 0L;
        }
        return (total - 1) / pageSize + 1;
    }

    /**
     * 本页是否无记录。
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PageResult<?> that)) {
            return false;
        }
        return currentPage == that.currentPage && pageSize == that.pageSize && total == that.total
                && items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, currentPage, pageSize, total);
    }

    /** 只回显分页元数据与记录条数，不展开元素内容（页可能很大，且 DTO 字段可能敏感）。 */
    @Override
    public String toString() {
        return "PageResult{currentPage=" + currentPage + ", pageSize=" + pageSize + ", total=" + total
                + ", pages=" + getPages() + ", items=" + items.size() + "}";
    }
}
