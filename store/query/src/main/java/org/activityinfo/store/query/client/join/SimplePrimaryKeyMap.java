package org.activityinfo.store.query.client.join;

import com.google.gwt.core.client.JavaScriptObject;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.shared.join.PrimaryKeyMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple PrimaryKeyMap that can be compiled to JavaScript
 */
public class SimplePrimaryKeyMap implements PrimaryKeyMap {

    private Map<String, Integer> map = new HashMap<>();

    public SimplePrimaryKeyMap(ColumnView idColumn) {
        for (int i = 0; i < idColumn.numRows(); i++) {
            map.put(idColumn.getString(i), i);
        }
    }

    @Override
    public int numRows() {
        return map.size();
    }

    @Override
    public int getRowIndex(String recordId) {
        Integer rowIndex = map.get(recordId);
        if(rowIndex == null) {
            return -1;
        }
        return rowIndex;
    }

    @Override
    public boolean contains(String recordId) {
        return map.containsKey(recordId);
    }
}
