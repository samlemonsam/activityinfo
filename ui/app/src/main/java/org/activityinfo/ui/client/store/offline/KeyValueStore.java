package org.activityinfo.ui.client.store.offline;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
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
        impl.putJson(status.toJson().toString(), CURRENT_SNAPSHOT_KEY);
    }

    public final void put(Set<ResourceId> offlineForms) {
        impl.putJson(toJson(offlineForms), OFFLINE_FORMS);
    }

    private String toJson(Set<ResourceId> offlineForms) {
        JsonArray array = new JsonArray();
        for (ResourceId offlineForm : offlineForms) {
            array.add(new JsonPrimitive(offlineForm.asString()));
        }
        return array.toString();
    }

    public final Promise<SnapshotStatus> getCurrentSnapshot() {
        return impl.getJson(CURRENT_SNAPSHOT_KEY).then(json -> {
            if(json == null) {
                return SnapshotStatus.EMPTY;
            } else {
                return SnapshotStatus.fromJson(json);
            }
        });
    }

    public final Promise<Set<ResourceId>> getOfflineForms() {
        return impl.getJson(OFFLINE_FORMS).then(json -> {
            if(json == null) {
                return Collections.emptySet();
            } else {
                Set<ResourceId> forms = new HashSet<>();
                JsonArray array = new JsonParser().parse(json).getAsJsonArray();
                for (JsonElement jsonElement : array) {
                    forms.add(ResourceId.valueOf(jsonElement.getAsString()));
                }
                return forms;
            }
        });
    }

}
