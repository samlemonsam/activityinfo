package org.activityinfo.ui.client.store.offline;

import org.activityinfo.indexedb.IDBDatabaseUpgrade;
import org.activityinfo.indexedb.IDBObjectStore;
import org.activityinfo.indexedb.ObjectStoreDefinition;
import org.activityinfo.indexedb.ObjectStoreOptions;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

/**
 * Stores the latest version and permissions of forms
 */
public class FormMetadataStore {

    public static final ObjectStoreDefinition<FormMetadataStore> DEF = new ObjectStoreDefinition<FormMetadataStore>() {
        @Override
        public String getName() {
            return "formMetadata";
        }

        @Override
        public void upgrade(IDBDatabaseUpgrade database, int oldVersion) {
            if(oldVersion < 1) {
                database.createObjectStore(getName(), ObjectStoreOptions.withDefaults());
            }
        }

        @Override
        public FormMetadataStore wrap(IDBObjectStore store) {
            return new FormMetadataStore(store);
        }
    };

    private IDBObjectStore<FormMetadataObject> impl;

    private FormMetadataStore(IDBObjectStore impl) {
        this.impl = impl;
    }

    public void put(FormMetadata formMetadata) {
        impl.put(formMetadata.getId().asString(), FormMetadataObject.from(formMetadata));
    }

    public Promise<FormMetadataObject> get(ResourceId formId) {
        return impl.get(formId.asString());
    }
}
