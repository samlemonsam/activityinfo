package org.activityinfo.service.store;

import org.activityinfo.model.query.ColumnView;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
* Provides caching of constructed columns
*/
public interface ColumnCache {

    public static final ColumnCache NULL = new ColumnCache() {
        @Override
        public Map<String, ColumnView> getIfPresent(Set<String> strings) {
            return Collections.emptyMap();
        }

        @Override
        public void put(Map<String, ColumnView> columnMap) {
        }
    };

    Map<String, ColumnView> getIfPresent(Set<String> strings);

    void put(Map<String, ColumnView> columnMap);
}
