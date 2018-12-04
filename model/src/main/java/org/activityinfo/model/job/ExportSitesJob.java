package org.activityinfo.model.job;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

public class ExportSitesJob implements JobDescriptor<ExportResult> {

    public static final String TYPE = "exportSites";

    private String filter = new String();

    ExportSitesJob() {
    }

    public ExportSitesJob(String filter) {
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

    public static ExportSitesJob fromJson(JsonValue object) {
        assert object.hasKey("filter") : "No filter defined for site export";
        return new ExportSitesJob(object.get("filter").asString());
    }

}
