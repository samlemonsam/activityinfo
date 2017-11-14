package org.activityinfo.model.job;

import org.activityinfo.json.JsonValue;

import static org.activityinfo.json.Json.createObject;

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

    public JsonValue toJsonObject() {
        JsonValue object = createObject();
        object.put("type", descriptor.getType());
        object.put("descriptor", descriptor.toJsonObject());
        if(locale != null) {
            object.put("locale", locale);
        }
        return object;
    }

    public JobDescriptor getDescriptor() {
        return descriptor;
    }

    public String getLocale() {
        return locale;
    }

    public static JobRequest fromJson(JsonValue object) {
        String type = object.get("type").asString();
        JobDescriptor descriptor = parseDescriptor(type, object.get("descriptor"));
        String locale = null;
        if(object.hasKey("locale")) {
            locale = object.get("locale").asString();
        }
        return new JobRequest(descriptor, locale);
    }

    public static JobDescriptor parseDescriptor(String type, JsonValue descriptor) {
        switch (type) {
            case ExportFormJob.TYPE:
                return ExportFormJob.fromJson(descriptor);
            case ExportAuditLog.TYPE:
                return ExportAuditLog.fromJson(descriptor);

        }
        throw new IllegalArgumentException(type);
    }
}
