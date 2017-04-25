package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.FormClass;


public class NullOfflineStore implements OfflineStore {
    @Override
    public void putSchema(FormClass formSchema) {
        // no op
    }
}
