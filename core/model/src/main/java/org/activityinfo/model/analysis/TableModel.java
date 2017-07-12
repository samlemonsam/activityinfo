package org.activityinfo.model.analysis;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

/**
 * The user's table model
 */
@Value.Immutable
public abstract class TableModel implements AnalysisModel  {

    private static final String TYPE = "table";

    public abstract ResourceId getFormId();

    public abstract List<TableColumn> getColumns();

    @Value.Lazy
    @Override
    public String getTypeId() {
        return TYPE;
    }

    @Value.Lazy
    @Override
    public JsonObject toJson() {


        JsonObject object = Json.createObject();
        object.put("formId", getFormId().asString());

        if(getColumns().size() != 0) {
            JsonArray columnArray = Json.createArray();
            for (TableColumn tableColumn : getColumns()) {
                columnArray.add(tableColumn.toJson());
            }
            object.put("columns", columnArray);
        }

        return object;
    }

    public static TableModel fromJson(JsonObject object) {
        ImmutableTableModel.Builder model = ImmutableTableModel.builder();
        model.formId(ResourceId.valueOf(object.getString("formId")));

        if(object.hasKey("columns")) {
            JsonArray columnArray = object.getArray("columns");
            for (int i = 0; i < columnArray.length(); i++) {
                model.addColumns(TableColumn.fromJson(object));
            }
        }
        return model.build();
    }
}
