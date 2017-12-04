package org.activityinfo.ui.client.table.view;

import com.google.common.base.Optional;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.analysis.table.SelectionViewModel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.SubscriptionSet;
import org.activityinfo.ui.client.input.view.FormDialog;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.table.ColumnDialog;

import java.util.logging.Logger;

public class TableToolBar extends ToolBar {

    private static final Logger LOGGER = Logger.getLogger(TableToolBar.class.getName());

    private FormStore formStore;
    private TableViewModel viewModel;

    private final TextButton editButton;
    private final TextButton removeButton;
    private final TextButton newButton;
    private final TextButton printButton;

    private final SubscriptionSet subscriptions = new SubscriptionSet();
    private final TextButton importButton;

    private ExportOptionsDialog exportOptionsDialog;


    public TableToolBar(FormStore formStore, TableViewModel viewModel) {
        this.formStore = formStore;
        this.viewModel = viewModel;

        newButton = new TextButton(I18N.CONSTANTS.newText());
        newButton.addSelectHandler(this::onNewRecord);

        removeButton = new TextButton(I18N.CONSTANTS.remove());
        removeButton.addSelectHandler(this::onDeleteRecord);
        removeButton.setEnabled(false);

        editButton = new TextButton(I18N.CONSTANTS.edit());
        editButton.addSelectHandler(this::onEditRecord);
        editButton.setEnabled(false);

        printButton = new TextButton(I18N.CONSTANTS.printForm());
        printButton.addSelectHandler(this::onPrintRecord);

        importButton = new TextButton(I18N.CONSTANTS.importText());
        importButton.addSelectHandler(this::onImport);

        TextButton exportButton = new TextButton(I18N.CONSTANTS.export());
        exportButton.addSelectHandler(this::onExport);

        TextButton columnsButton = new TextButton(I18N.CONSTANTS.chooseColumns());
        columnsButton.addSelectHandler(this::onChooseColumnsSelected);

        OfflineStatusButton offlineButton = new OfflineStatusButton(formStore, viewModel.getFormId());

        add(newButton);
        add(editButton);
        add(removeButton);
        add(importButton);
        add(exportButton);
        add(columnsButton);
        add(offlineButton);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        subscriptions.add(viewModel.getFormTree().subscribe(this::onFormTreeChanged));
        subscriptions.add(viewModel.getSelectionViewModel().subscribe(this::onSelectedRecordChanged));
    }

    private void onFormTreeChanged(Observable<FormTree> tree) {
        boolean canCreate = isNewAllowed(tree);
        newButton.setEnabled(canCreate);
        importButton.setEnabled(canCreate);
    }

    private boolean isNewAllowed(Observable<FormTree> tree) {
        if(tree.isLoading()) {
            return false;
        }
        FormMetadata rootForm = tree.get().getRootMetadata();
        return rootForm.getPermissions().isCreateAllowed();
    }

    @Override
    protected void onDetach() {
        super.onDetach();

        subscriptions.unsubscribeAll();
    }

    private void onSelectedRecordChanged(Observable<Optional<SelectionViewModel>> selection) {
        boolean hasSelection = false;
        boolean canEdit = false;
        boolean canDelete = false;

        if(selection.isLoaded() && selection.get().isPresent()) {
            hasSelection = true;
            SelectionViewModel viewModel = selection.get().get();
            canEdit = viewModel.isEditAllowed();
            canDelete = viewModel.isDeleteAllowed();
        }

        editButton.setEnabled(hasSelection && canEdit);
        removeButton.setEnabled(hasSelection && canDelete);

        if(hasSelection && !canEdit) {
            editButton.setToolTip("You do not have permission to edit the selection");
        } else {
            editButton.removeToolTip();
        }

        if(hasSelection && !canDelete) {
            removeButton.setToolTip("You do not have permission to delete the selection");
        } else {
            removeButton.removeToolTip();
        }
    }

    private void onNewRecord(SelectEvent event) {

        ResourceId newRecordId = ResourceId.generateSubmissionId(viewModel.getFormId());
        RecordRef newRecordRef = new RecordRef(viewModel.getFormId(), newRecordId);

        FormDialog dialog = new FormDialog(formStore, newRecordRef);
        dialog.show();
    }

    private void onEditRecord(SelectEvent event) {

        Optional<RecordRef> selected = viewModel.getSelectedRecordRef().get();
        if(!selected.isPresent()) {
            return;
        }

        FormDialog dialog = new FormDialog(formStore, selected.get());
        dialog.show();
    }

    private void onPrintRecord(SelectEvent event) {

    }

    private void onDeleteRecord(SelectEvent event) {

        Observable<FormTree> formTree = viewModel.getFormTree();
        Observable<Optional<RecordRef>> selection = viewModel.getSelectedRecordRef();

        // This button should not be enabled if both formTree and selection aren't
        // present and loaded, but check anyway
        if(formTree.isLoading() || selection.isLoading() || !selection.get().isPresent()) {
            return;
        }

        String formLabel = formTree.get().getRootFormClass().getLabel();

        ConfirmDialog.confirm(new DeleteRecordAction(formStore, formLabel, selection.get().get()));
    }

    private void onImport(SelectEvent event) {
        // Redirect to old app for the moment.
        UrlBuilder importUrl = Window.Location.createUrlBuilder();
        importUrl.setHash("#import/" + viewModel.getFormId().asString());
        importUrl.removeParameter("ui");

        Window.open(importUrl.buildString(), "_blank", null);
    }

    private void onExport(SelectEvent event) {

        if(exportOptionsDialog == null) {
            exportOptionsDialog = new ExportOptionsDialog(formStore, viewModel);
        }
        exportOptionsDialog.show();
    }


    private void onChooseColumnsSelected(SelectEvent event) {
        if(viewModel.getEffectiveTable().isLoaded()) {
            ColumnDialog dialog = new ColumnDialog(viewModel.getEffectiveTable().get());
            dialog.show(updatedModel -> {
                viewModel.update(updatedModel);
            });
        }
    }
}

