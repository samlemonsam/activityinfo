package org.activityinfo.model.analysis;

import com.google.common.base.Optional;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;

import java.util.List;

/**
 * The user's table model
 */
@Value.Immutable
public abstract class TableModel implements AnalysisModel  {

    private static final String TYPE = "table";

    public abstract ResourceId getFormId();

    public abstract List<TableColumn> getColumns();

    public abstract List<SortOrder> getOrdering();

    /**
     * A boolean-valued formula that determines which rows to include in the
     * results.
     */
    public abstract Optional<String> getFilter();

    @Value.Lazy
    @Override
    public String getTypeId() {
        return TYPE;
    }

    @Value.Lazy
    @Override
    public JsonValue toJson() {


        JsonValue object = Json.createObject();
        object.put("formId", getFormId().asString());

        if(getColumns().size() != 0) {
            JsonValue columnArray = Json.createArray();
            for (TableColumn tableColumn : getColumns()) {
                columnArray.add(tableColumn.toJson());
            }
            object.put("columns", columnArray);
        }

        return object;
    }

    public static TableModel fromJson(JsonValue object) {
        ImmutableTableModel.Builder model = ImmutableTableModel.builder();
        model.formId(ResourceId.valueOf(object.getString("formId")));

        if(object.hasKey("columns")) {
            JsonValue columnArray = object.get("columns");
            for (int i = 0; i < columnArray.length(); i++) {
                model.addColumns(TableColumn.fromJson(columnArray.get(i)));
            }
        }
        return model.build();
    }
}
