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
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

import java.util.Optional;

/**
 * IndexedDB object store for form schemas.
 */
public class SchemaStore {

    public static final ObjectStoreDefinition<SchemaStore> DEF = new ObjectStoreDefinition<SchemaStore>() {
        @Override
        public String getName() {
            return "schemas";
        }

        @Override
        public void upgrade(IDBDatabaseUpgrade database, int oldVersion) {
            if(oldVersion < 1) {
                database.createObjectStore(getName(), ObjectStoreOptions.withKey("id"));
            }
        }

        @Override
        public SchemaStore wrap(IDBObjectStore store) {
            return new SchemaStore(store);
        }
    };

    private IDBObjectStore<JsonValue> impl;

    private SchemaStore(IDBObjectStore<JsonValue> impl) {
        this.impl = impl;
    }

    public final void put(FormClass formClass) {
        impl.put(formClass.toJson());
    }

    public final Promise<Optional<FormClass>> get(ResourceId formId) {
        return impl.get(formId.asString()).then(json -> {
            if(json == null) {
                return Optional.empty();
            } else {
                return Optional.of(FormClass.fromJson(json));
            }
        });
    }

}