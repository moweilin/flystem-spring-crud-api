package com.flystem.crud.api.link;

import com.flystem.crud.api.page.PageResult;
import java.util.ArrayList;
import java.util.List;

/** {@link RelationshipLoader#collectRefs(Object)} 的包内实现。 */
final class RelationRefCollector {

    private RelationRefCollector() {
    }

    static List<RelationRef> collect(Object data) {
        if (data == null) {
            return List.of();
        }
        List<RelationRef> out = new ArrayList<>();
        collectInto(data, out);
        return List.copyOf(out);
    }

    private static void collectInto(Object data, List<RelationRef> out) {
        if (data == null) {
            return;
        }
        if (data instanceof HasRelations hasRelations) {
            List<RelationRef> refs = hasRelations.relationRefs();
            if (refs != null && !refs.isEmpty()) {
                out.addAll(refs);
            }
            return;
        }
        if (data instanceof PageResult<?> page) {
            for (Object item : page.getItems()) {
                collectInto(item, out);
            }
            return;
        }
        if (data instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                collectInto(item, out);
            }
        }
    }
}
