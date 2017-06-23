package org.activityinfo.model.job;

import org.activityinfo.json.JsonObject;

import static org.activityinfo.json.Json.createObject;

public class ExportAuditLog implements JobDescriptor<ExportResult> {

    public static final String TYPE = "exportAuditLog";

    private int databaseId;

    public ExportAuditLog(int databaseId) {
        this.databaseId = databaseId;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public ExportResult parseResult(org.activityinfo.json.JsonObject resultObject) {
        return ExportResult.fromJson(resultObject);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject object = createObject();
        object.put("databaseId", databaseId);
        return object;
    }

    public static ExportAuditLog fromJson(org.activityinfo.json.JsonObject object) {
        return new ExportAuditLog(object.get("databaseId").asInt());
    }

    public int getDatabaseId() {
        return databaseId;
    }
}
