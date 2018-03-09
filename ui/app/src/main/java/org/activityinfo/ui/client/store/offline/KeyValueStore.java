/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.store.offline;

import org.activityinfo.indexedb.IDBDatabaseUpgrade;
import org.activityinfo.indexedb.IDBObjectStore;
import org.activityinfo.indexedb.ObjectStoreDefinition;
import org.activityinfo.indexedb.ObjectStoreOptions;
import org.activityinfo.json.Json;
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

    public static final ObjectStoreDefinition<KeyValueStore> DEF = new ObjectStoreDefinition<KeyValueStore>() {
        @Override
        public String getName() {
            return "values";
        }

        @Override
        public void upgrade(IDBDatabaseUpgrade database, int oldVersion) {
            if(oldVersion < 1) {
                database.createObjectStore(getName(), ObjectStoreOptions.withDefaults());
            }
        }

        @Override
        public KeyValueStore wrap(IDBObjectStore store) {
            return new KeyValueStore(store);
        }
    };

    private static final String CURRENT_SNAPSHOT_KEY = "snapshot";

    private static final String OFFLINE_FORMS = "offlineForms";

    private final IDBObjectStore<JsonValue> impl;

    private KeyValueStore(IDBObjectStore impl) {
        this.impl = impl;
    }

    public final void put(SnapshotStatus status) {
        impl.put(CURRENT_SNAPSHOT_KEY, status.toJson());
    }

    public final void put(Set<ResourceId> offlineForms) {
        impl.put(OFFLINE_FORMS, toJson(offlineForms));
    }

    private JsonValue toJson(Set<ResourceId> offlineForms) {
        JsonValue array = Json.createArray();
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
                JsonValue array = json;
                for (int i = 0; i < array.length(); i++) {
                    forms.add(ResourceId.valueOf(array.getString(i)));
                }
                return forms;
            }
        });
    }

}
