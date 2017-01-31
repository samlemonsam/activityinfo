package org.activityinfo.ui.client.component.chooseForm;

import org.activityinfo.model.form.CatalogEntry;

/**
 * Calls back from the {@link ChooseFormDialog}
 */
public interface ChooseFormCallback {

    void onChosen(CatalogEntry entry);

    void onCanceled();

}
