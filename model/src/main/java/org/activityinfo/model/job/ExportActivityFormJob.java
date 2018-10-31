package org.activityinfo.model.job;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

public class ExportActivityFormJob implements JobDescriptor<ExportResult> {

    public static final String TYPE = "exportActivityForm";

    private String filter = new String();

    ExportActivityFormJob() {
    }

    public ExportActivityFormJob(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public ExportResult parseResult(JsonValue resultObject) {
        return ExportResult.fromJson(resultObject);
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("filter", filter);
        return object;
    }

    public static ExportActivityFormJob fromJson(JsonValue object) {
        assert object.hasKey("filter") : "No filter defined for activity export";
        return new ExportActivityFormJob(object.get("filter").asString());
    }

}
