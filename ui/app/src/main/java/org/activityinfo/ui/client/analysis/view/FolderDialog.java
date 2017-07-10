package org.activityinfo.ui.client.analysis.view;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.measureDialog.view.CatalogTreeView;
import org.activityinfo.ui.client.store.FormStore;

import javax.annotation.Nullable;

/**
 * Allows a user to choose a folder
 */
public class FolderDialog {

    private Dialog dialog;
    private final CatalogTreeView treeView;

    public FolderDialog(FormStore formStore) {
        dialog = new Dialog();
        dialog.setPixelSize(350, 450);
        dialog.setModal(true);
        dialog.setHeading("Choose a folder");

        treeView = new CatalogTreeView(formStore, Optional.absent(), new Predicate<CatalogEntry>() {
            @Override
            public boolean apply(@Nullable CatalogEntry entry) {
                return !entry.getId().equals("geodb") && entry.getType() == CatalogEntryType.FOLDER;
            }
        });
        dialog.setWidget(treeView);
        dialog.setPredefinedButtons(Dialog.PredefinedButton.OK, Dialog.PredefinedButton.CANCEL);

        dialog.getButton(Dialog.PredefinedButton.CANCEL).addSelectHandler(event -> dialog.hide());

        treeView.getSelectedEntry().subscribe(this::selectionChanged);
    }

    public CatalogEntry getSelected() {
        return treeView.getSelectedEntry().get().get();
    }


    private void selectionChanged(Observable<Optional<CatalogEntry>> selectedEntry) {
        dialog.getButton(Dialog.PredefinedButton.OK).setEnabled(
            selectedEntry.isLoaded() &&
            selectedEntry.get().isPresent());
    }

    public void show() {
        dialog.show();
    }

    public SelectEvent.HasSelectHandlers getOkButton() {
        return dialog.getButton(Dialog.PredefinedButton.OK);
    }

    public void hide() {
        dialog.hide();
    }
}
