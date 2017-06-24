package org.activityinfo.ui.client.store.offline;

import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.activityinfo.json.Json.createObject;

/**
 * Status information the current offline snapshot.
 */
public class SnapshotStatus {

    public static final SnapshotStatus EMPTY = new SnapshotStatus();

    private Date time = new Date();
    private Map<ResourceId, Long> formVersions = new HashMap<>();

    private SnapshotStatus() {
    }

    public SnapshotStatus(Snapshot snapshot) {
        for (FormMetadata metadata : snapshot.getForms()) {
            formVersions.put(metadata.getId(), metadata.getVersion());
        }
    }

    public boolean isEmpty() {
        return formVersions.isEmpty();
    }

    public Date getSnapshotTime() {
        return time;
    }

    public boolean isFormCached(ResourceId formId) {
        return formVersions.containsKey(formId);
    }

    public JsonObject toJson() {
        JsonObject versions = createObject();
        for (Map.Entry<ResourceId, Long> entry : formVersions.entrySet()) {
            versions.put(entry.getKey().asString(), Long.toString(entry.getValue()));
        }

        JsonObject object = createObject();
        object.put("time", time.getTime());
        object.put("versions", versions);
        return object;
    }

    public static SnapshotStatus fromJson(JsonObject object) {
        SnapshotStatus status = new SnapshotStatus();
        status.time = new Date(object.get("time").asLong());

        JsonObject versions = object.getObject("versions");
        for (Map.Entry<String, JsonValue> entry : versions.entrySet()) {
            ResourceId formId = ResourceId.valueOf(entry.getKey());
            long version = entry.getValue().asLong();
            status.formVersions.put(formId, version);
        }

        return status;
    }

    public static SnapshotStatus fromJson(JsonValue json) {
        return fromJson(json.getAsJsonObject());
    }

    @Override
    public String toString() {
        return "SnapshotStatus{" +
                "time=" + time +
                ", formVersions=" + formVersions +
                '}';
    }
}
