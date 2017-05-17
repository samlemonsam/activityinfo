package org.activityinfo.model.job;

import com.google.gson.JsonObject;

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
    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("downloadUrl", downloadUrl);
        return object;
    }

    public static ExportResult fromJson(JsonObject resultObject) {
        return new ExportResult(resultObject.get("downloadUrl").getAsString());
    }
}
