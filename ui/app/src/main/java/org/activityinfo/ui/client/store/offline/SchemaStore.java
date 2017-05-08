package org.activityinfo.ui.client.store.offline;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

/**
 * IndexedDB object store for form schemas.
 */
public class SchemaStore extends ObjectStore {

    public static final String NAME = "formSchemas";

    protected SchemaStore() {
    }

    public final void put(FormClass formClass) {
        putJson(formClass.toJsonString());
    }

    public final Promise<FormClass> get(ResourceId formId) {
        return getJson(formId.asString()).then(FormClass::fromJson);
    }

}