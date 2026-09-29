package com.flystem.crud.api.wrap;

import com.flystem.crud.api.link.LinkView;
import com.flystem.crud.api.link.RelationRef;
import com.flystem.crud.api.link.RelationshipLoader;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * {@link ResultWrapper} 的默认实现：执行 supplier，经 {@link RelationshipLoader} <b>按类型批量</b>
 * 解析 {@code data} 上的关联引用，装进 {@link R#ok(Object, Map)}。
 *
 * <p><b>刻意不吞异常</b>：把异常翻译成 HTTP 200 + 失败码是有争议的选择（它对监控、网关重试、
 * 客户端错误分支都不友好），库不该替使用方定这件事。默认实现因此让异常照常抛出，交由使用方的
 * {@code @ControllerAdvice} / 网关处理。要「异常 → 失败码」的语义，声明自己的 {@link ResultWrapper}
 * bean 即可整体替换（{@code @ConditionalOnMissingBean}）；自定义实现若仍要自动关联，应自行注入并调用
 * {@link RelationshipLoader}。
 *
 * <p>换成 {@code Supplier} 形态（而不是直接收已算好的值）正是为了让这类替换成为可能：实现能在业务
 * 调用<b>前后</b>插入逻辑，生成产物无需改动。
 */
public class DefaultResultWrapper implements ResultWrapper {

    private final RelationshipLoader relationshipLoader;

    /** 无加载器：只包装 data，{@code relationships} 恒为空。 */
    public DefaultResultWrapper() {
        this(null);
    }

    /**
     * @param relationshipLoader 关联批量加载器；{@code null} 时不解析关联
     */
    public DefaultResultWrapper(RelationshipLoader relationshipLoader) {
        this.relationshipLoader = relationshipLoader;
    }

    @Override
    public <T> R<T> wrap(Supplier<T> supplier) {
        T data = supplier.get();
        if (relationshipLoader == null) {
            return R.ok(data);
        }
        List<RelationRef> refs = RelationshipLoader.collectRefs(data);
        if (refs.isEmpty()) {
            return R.ok(data);
        }
        Map<String, List<LinkView>> relationships = relationshipLoader.resolve(refs);
        return R.ok(data, relationships);
    }
}
