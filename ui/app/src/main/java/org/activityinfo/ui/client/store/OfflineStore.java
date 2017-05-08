package org.activityinfo.ui.client.store;

import com.google.gwt.user.client.rpc.AsyncCallback;
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
    void loadSchema(ResourceId resourceId, AsyncCallback<FormClass> formSchema);

    /**
     * Updates whether a form should be available offline.
     */
    void enableOffline(ResourceId formId, boolean offline);
}
