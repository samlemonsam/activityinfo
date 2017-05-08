package org.activityinfo.ui.client.store.offline;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

/**
 * IndexedDB object store for form schemas.
 */
public class SchemaStore {

    public static final String NAME = "formSchemas";

    private IDBObjectStore impl;

    SchemaStore(IDBObjectStore impl) {
        this.impl = impl;
    }

    public final void put(FormClass formClass) {
        impl.putJson(formClass.toJsonString());
    }

    public final Promise<FormClass> get(ResourceId formId) {
        return impl.getJson(formId.asString()).then(FormClass::fromJson);
    }

}