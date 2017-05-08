package org.activityinfo.ui.client.store;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;


public class IndexedDBOfflineStore implements OfflineStore {

    @Override
    public void putSchema(FormClass formSchema) {
        Promise<Void> result = IndexedDB.open().join(db -> db.putSchema(formSchema));
    }

    @Override
    public void loadSchema(ResourceId formId, AsyncCallback<FormClass> callback) {
        IndexedDB.open().join(db -> db.loadSchema(formId)).then(callback);
    }

    @Override
    public void enableOffline(ResourceId formId, boolean offline) {

    }
}
