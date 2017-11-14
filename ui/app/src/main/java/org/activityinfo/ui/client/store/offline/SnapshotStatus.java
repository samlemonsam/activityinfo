package org.activityinfo.ui.client.store.offline;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public JsonValue toJson() {
        JsonValue versions = createObject();
        for (Map.Entry<ResourceId, Long> entry : formVersions.entrySet()) {
            versions.put(entry.getKey().asString(), Long.toString(entry.getValue()));
        }

        JsonValue object = createObject();
        object.put("time", time.getTime());
        object.put("versions", versions);
        return object;
    }

    public static SnapshotStatus fromJson(JsonValue object) {
        SnapshotStatus status = new SnapshotStatus();
        status.time = new Date(object.get("time").asLong());

        JsonValue versions = object.get("versions");
        String[] forms = versions.keys();

        for (String form : forms) {
            ResourceId formId = ResourceId.valueOf(form);
            long version = Long.parseLong(versions.getString(form));

            status.formVersions.put(formId, version);
        }

        return status;
    }

    @Override
    public String toString() {
        return "SnapshotStatus{" +
                "time=" + time +
                ", formVersions=" + formVersions +
                '}';
    }

    public boolean areAllCached(Set<ResourceId> formIds) {
        for (ResourceId formId : formIds) {
            if(!isFormCached(formId)) {
                return false;
            }
        }
        return true;
    }
}
