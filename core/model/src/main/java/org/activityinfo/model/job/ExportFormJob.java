package org.activityinfo.model.job;

import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;

/**
 * Exports a single form to a CSV table
 */
public class ExportFormJob implements JobDescriptor<ExportResult> {

    public static final String TYPE = "exportForm";

    private ResourceId formId;

    public ExportFormJob(ResourceId formId) {
        this.formId = formId;
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
        object.addProperty("formId", formId.asString());
        return object;
    }

    public static ExportFormJob fromJson(JsonObject object) {
        return new ExportFormJob(ResourceId.valueOf(object.get("formId").getAsString()));
    }
}
