package org.activityinfo.ui.client.store.offline;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        JsonObject versions = new JsonObject();
        for (Map.Entry<ResourceId, Long> entry : formVersions.entrySet()) {
            versions.addProperty(entry.getKey().asString(), Long.toString(entry.getValue()));
        }

        JsonObject object = new JsonObject();
        object.addProperty("time", time.getTime());
        object.add("versions", versions);
        return object;
    }

    public static SnapshotStatus fromJson(JsonObject object) {
        SnapshotStatus status = new SnapshotStatus();
        status.time = new Date(object.get("time").getAsLong());

        JsonObject versions = object.getAsJsonObject("versions");
        for (Map.Entry<String, JsonElement> entry : versions.entrySet()) {
            ResourceId formId = ResourceId.valueOf(entry.getKey());
            long version = entry.getValue().getAsLong();
            status.formVersions.put(formId, version);
        }

        return status;
    }

    public static SnapshotStatus fromJson(String json) {
        return fromJson(new JsonParser().parse(json).getAsJsonObject());
    }

    @Override
    public String toString() {
        return "SnapshotStatus{" +
                "time=" + time +
                ", formVersions=" + formVersions +
                '}';
    }
}
