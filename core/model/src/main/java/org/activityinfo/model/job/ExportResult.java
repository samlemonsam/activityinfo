package org.activityinfo.model.job;

import org.activityinfo.json.JsonValue;

import static org.activityinfo.json.Json.createObject;

/**
 * Result for a job that results in a download.
 */
public class ExportResult implements JobResult {

    private String downloadUrl;

    public ExportResult(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public JsonValue toJsonObject() {
        JsonValue object = createObject();
        object.put("downloadUrl", downloadUrl);
        return object;
    }

    public static ExportResult fromJson(JsonValue resultObject) {
        return new ExportResult(resultObject.get("downloadUrl").asString());
    }
}
