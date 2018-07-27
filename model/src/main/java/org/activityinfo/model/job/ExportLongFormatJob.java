package org.activityinfo.model.job;


import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

/**
 * Long Format export of all forms in a database to a CSV table
 */
public class ExportLongFormatJob implements JobDescriptor<ExportResult> {

    public static  final String TYPE = "exportLongFormat";

    private int databaseId;

    public ExportLongFormatJob(int databaseId) {
        this.databaseId = databaseId;
    }

    public int getDatabaseId() {
        return databaseId;
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
        object.put("databaseId", databaseId);
        return object;
    }

    public static ExportLongFormatJob fromJson(JsonValue object) {
        if (!object.hasKey("databaseId")) {
            throw new IllegalArgumentException("Database Id not defined");
        }
        int databaseId = object.get("databaseId").asInt();
        return new ExportLongFormatJob(databaseId);

    }

}
