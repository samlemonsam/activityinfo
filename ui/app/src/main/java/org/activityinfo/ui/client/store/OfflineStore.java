package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

/**
 * Interface to *something* that can store stuff offline.
 */
public interface OfflineStore {

    void putSchema(FormClass formSchema);

    /**
     * Try to load a cached FormSchema from the offline store.
     */
    void loadSchema(ResourceId resourceId, CallbackMaybe<FormClass> formSchema);
}
