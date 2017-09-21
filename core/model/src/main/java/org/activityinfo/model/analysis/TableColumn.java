package org.activityinfo.model.analysis;


import com.google.common.base.Optional;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;


@Value.Immutable
public abstract class TableColumn {

    @Value.Default
    public String getId() {
        return ResourceId.generateCuid();
    }

    public abstract Optional<String> getLabel();

    /**
     * @return the width of the column in pixels.
     */
    public abstract Optional<Integer> getWidth();

    public abstract String getFormula();

    @Value.Lazy
    public JsonValue toJson() {
        JsonObject object = Json.createObject();
        object.put("id", getId());
        if(getLabel().isPresent()) {
            object.put("label", getLabel().get());
        }
        object.put("formula", getFormula());
        return object;
    }

    public static TableColumn fromJson(JsonObject object) {
        ImmutableTableColumn.Builder tableColumn = ImmutableTableColumn.builder();
        tableColumn.id(object.getString("id"));
        if(object.hasKey("label")) {
            tableColumn.label(object.getString("label"));
        }
        tableColumn.formula(object.getString("formula"));
        return tableColumn.build();
    }
}
