package org.activityinfo.ui.client.component.table;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonParser;
import org.activityinfo.json.JsonValue;
import org.activityinfo.ui.client.dispatch.state.StateProvider;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuriyz on 5/30/2016.
 */
public class ColumnStatePersister {

    private static final String INSTANCE_TABLE_COLUMNS_STATE = "instanceTableColumnStatev2";

    private StateProvider stateProvider;

    public ColumnStatePersister(StateProvider stateProvider) {
        this.stateProvider = stateProvider;
    }

    public void persist(List<FieldColumn> columns) {
        persist(FieldColumn.headers(columns));
    }

    /**
     * Persists column names in given order (LinkedHashSet preserve order)
     *
     * @param columnNames column names.
     */
    public void persist(LinkedHashSet<String> columnNames) {
        if (columnNames.isEmpty()) {
            return;
        }

        stateProvider.set(INSTANCE_TABLE_COLUMNS_STATE, asRecord(columnNames).toString());
    }

    public Set<String> getColumnNames() {
        Set<String> columns = Sets.newLinkedHashSet();
        String json = stateProvider.getString(INSTANCE_TABLE_COLUMNS_STATE);
        if (!Strings.isNullOrEmpty(json)) {
            JsonParser parser = new JsonParser();
            JsonObject record = parser.parse(json).getAsJsonObject();
            for (Map.Entry<String, JsonValue> entry : record.entrySet()) {
                columns.add(entry.getKey());
            }
        }
        return columns;
    }

    private org.activityinfo.json.JsonObject asRecord(LinkedHashSet<String> columnNames) {
        JsonObject record = Json.createObject();
        for (String columnName : columnNames) {
            record.put(columnName, columnName);
        }
        return record;
    }
}
