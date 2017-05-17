package org.activityinfo.model.job;

import com.google.gson.JsonObject;

/**
 * Request body to start a new job
 */
public class JobRequest {
    private JobDescriptor descriptor;
    private String locale;

    public JobRequest(JobDescriptor descriptor, String locale) {
        this.descriptor = descriptor;
        this.locale = locale;
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("type", descriptor.getType());
        object.add("descriptor", descriptor.toJsonObject());
        if(locale != null) {
            object.addProperty("locale", locale);
        }
        return object;
    }

    public JobDescriptor getDescriptor() {
        return descriptor;
    }

    public String getLocale() {
        return locale;
    }

    public static JobRequest fromJson(JsonObject object) {
        String type = object.get("type").getAsString();
        JobDescriptor descriptor = parseDescriptor(type, object.getAsJsonObject("descriptor"));
        String locale = null;
        if(object.has("locale")) {
            locale = object.get("locale").getAsString();
        }
        return new JobRequest(descriptor, locale);
    }

    public static JobDescriptor parseDescriptor(String type, JsonObject descriptor) {
        switch (type) {
            case ExportFormJob.TYPE:
                return ExportFormJob.fromJson(descriptor);

        }
        throw new IllegalArgumentException(type);
    }
}
