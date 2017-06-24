package org.activityinfo.ui.client.store.offline;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ObjectStore for simple key value pairs.
 */
public class KeyValueStore {

    public static final String NAME = "values";

    private static final String CURRENT_SNAPSHOT_KEY = "snapshot";

    private static final String OFFLINE_FORMS = "offlineForms";

    private final IDBObjectStore impl;

    KeyValueStore(IDBObjectStore impl) {
        this.impl = impl;
    }

    public final void put(SnapshotStatus status) {
        impl.put(CURRENT_SNAPSHOT_KEY, status.toJson());
    }

    public final void put(Set<ResourceId> offlineForms) {
        impl.put(OFFLINE_FORMS, toJson(offlineForms));
    }

    private JsonArray toJson(Set<ResourceId> offlineForms) {
        JsonArray array = Json.createArray();
        for (ResourceId offlineForm : offlineForms) {
            array.add(Json.create(offlineForm.asString()));
        }
        return array;
    }

    public final Promise<SnapshotStatus> getCurrentSnapshot() {
        return impl.get(CURRENT_SNAPSHOT_KEY).then(json -> {
            if(json == null) {
                return SnapshotStatus.EMPTY;
            } else {
                return SnapshotStatus.fromJson(json);
            }
        });
    }

    public final Promise<Set<ResourceId>> getOfflineForms() {
        return impl.get(OFFLINE_FORMS).then(json -> {
            if(json == null) {
                return Collections.emptySet();
            } else {
                Set<ResourceId> forms = new HashSet<>();
                JsonArray array = json.getAsJsonArray();
                for (JsonValue jsonElement : array.values()) {
                    forms.add(ResourceId.valueOf(jsonElement.asString()));
                }
                return forms;
            }
        });
    }

}
