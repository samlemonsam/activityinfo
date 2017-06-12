package org.activityinfo.model.job;

import com.google.gson.JsonObject;

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
    public ExportResult parseResult(JsonObject resultObject) {
        return ExportResult.fromJson(resultObject);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("databaseId", databaseId);
        return object;
    }

    public static ExportAuditLog fromJson(JsonObject object) {
        return new ExportAuditLog(object.get("databaseId").getAsInt());
    }

    public int getDatabaseId() {
        return databaseId;
    }
}
