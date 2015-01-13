package org.activityinfo.store.query.impl;

import com.google.common.base.Supplier;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
* Provides caching of constructed columns
*/
public interface ColumnCache {

    public static final ColumnCache NULL = new ColumnCache() {
        @Override
        public Map<String, ColumnView> getIfPresent(ResourceId id, Set<String> strings) {
            return Collections.emptyMap();
        }

        @Override
        public void put(ResourceId id, Map<String, ? extends Supplier<ColumnView>> columnMap) {
        }
    };

    Map<String, ColumnView> getIfPresent(ResourceId id, Set<String> strings);

    void put(ResourceId id, Map<String, ? extends Supplier<ColumnView>> columnMap);
}
