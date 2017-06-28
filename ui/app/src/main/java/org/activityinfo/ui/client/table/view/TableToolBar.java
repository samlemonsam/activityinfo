package org.activityinfo.ui.client.table.view;

import com.google.common.base.Optional;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.input.view.FormDialog;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.table.ColumnDialog;

public class TableToolBar extends ToolBar {

    private FormStore formStore;
    private TableViewModel viewModel;

    private final TextButton editButton;
    private final TextButton removeButton;
    private final TextButton newButton;
    private final TextButton printButton;
    private Subscription subscription;


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

        TextButton importButton = new TextButton(I18N.CONSTANTS.importText());
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

        subscription = viewModel.getSelectedRecordRef().subscribe(this::onSelectedRecordChanged);
    }

    @Override
    protected void onDetach() {
        super.onDetach();

        subscription.unsubscribe();
    }

    private void onSelectedRecordChanged(Observable<Optional<RecordRef>> selection) {
        boolean haveSelection = selection.isLoaded() && selection.get().isPresent();

        editButton.setEnabled(haveSelection);
        removeButton.setEnabled(haveSelection);
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

    }

    private void onExport(SelectEvent event) {
        TableModel tableModel = viewModel.getTableModel();
        ExportFormJob exportFormJob = new ExportFormJob(tableModel);

        Observable<JobStatus<ExportFormJob, ExportResult>> jobStatus = formStore.startJob(exportFormJob);
        ExportJobDialog dialog = new ExportJobDialog(jobStatus);
        dialog.show();
    }


    private void onChooseColumnsSelected(SelectEvent event) {
        if(viewModel.getEffectiveTable().isLoaded()) {
            ColumnDialog dialog = new ColumnDialog(viewModel.getEffectiveTable().get());
            dialog.show();
        }
    }
}
