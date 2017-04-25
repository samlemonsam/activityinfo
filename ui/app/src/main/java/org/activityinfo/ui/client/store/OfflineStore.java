package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.FormClass;

/**
 * Interface to *something* that can store stuff offline.
 */
public interface OfflineStore {

    void putSchema(FormClass formSchema);
}
