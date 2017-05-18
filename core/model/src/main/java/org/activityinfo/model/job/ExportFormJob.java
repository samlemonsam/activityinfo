package org.activityinfo.model.job;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;

import java.util.ArrayList;
import java.util.List;

/**
 * Exports a single form to a CSV table
 */
public class ExportFormJob implements JobDescriptor<ExportResult> {

    public static final String TYPE = "exportForm";

    private ResourceId formId;
    private List<ExportColumn> columns;

    public ExportFormJob(ResourceId formId, List<ExportColumn> columns) {
        this.formId = formId;
        this.columns = columns;
    }

    public ResourceId getFormId() {
        return formId;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<ExportColumn> getColumns() {
        return columns;
    }

    @Override
    public ExportResult parseResult(JsonObject resultObject) {
        return ExportResult.fromJson(resultObject);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("formId", formId.asString());
        return object;
    }

    public static ExportFormJob fromJson(JsonObject object) {

        List<ExportColumn> columns = new ArrayList<>();
        if(object.has("columns")) {
            for (JsonElement jsonElement : object.getAsJsonArray("columns").getAsJsonArray()) {
                columns.add(ExportColumn.fromJson(jsonElement.getAsJsonObject()));
            }
        }

        return new ExportFormJob(ResourceId.valueOf(object.get("formId").getAsString()), columns);
    }
}
