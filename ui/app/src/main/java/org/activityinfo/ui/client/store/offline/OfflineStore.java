package org.activityinfo.ui.client.store.offline;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.Snapshot;

import java.util.Set;

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

    /**
     * @return the set of forms that should be made available offline.
     */
    Observable<Set<ResourceId>> getOfflineForms();

    /**
     * Stores a new snapshot to the remote store
     */
    void store(Snapshot snapshot);
}
