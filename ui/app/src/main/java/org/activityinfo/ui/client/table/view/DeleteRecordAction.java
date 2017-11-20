package org.activityinfo.ui.client.table.view;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.RecordTransactionBuilder;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormStore;

import java.util.Collections;
import java.util.Set;

/**
 * Deletes one or more records.
 */
public class DeleteRecordAction implements ConfirmDialog.Action {

    private FormStore formStore;
    private final Set<RecordRef> selection;
    private String formLabel;

    public DeleteRecordAction(FormStore formStore, String formLabel, Set<RecordRef> records) {
        this.formStore = formStore;
        this.formLabel = formLabel;
        this.selection = records;
    }

    public DeleteRecordAction(FormStore formStore, String formLabel, RecordRef record) {
        this(formStore, formLabel, Collections.singleton(record));
    }


    @Override
    public ConfirmDialog.Messages getConfirmationMessages() {
        return new ConfirmDialog.Messages(
                I18N.CONSTANTS.confirmDeletion(),
                I18N.MESSAGES.removeTableRowsConfirmation(selection.size(), formLabel),
                I18N.CONSTANTS.delete());
    }

    @Override
    public ConfirmDialog.Messages getProgressMessages() {
        return new ConfirmDialog.Messages(
                I18N.CONSTANTS.deletionInProgress(),
                I18N.MESSAGES.deletingRows(selection.size(), formLabel),
                I18N.CONSTANTS.deleting());
    }

    @Override
    public ConfirmDialog.Messages getFailureMessages() {
        return new ConfirmDialog.Messages(
                I18N.CONSTANTS.deletionFailed(),
                I18N.MESSAGES.retryDeletion(selection.size(), formLabel),
                I18N.CONSTANTS.retry());
    }

    @Override
    public Promise<Void> execute() {

        RecordTransactionBuilder tx = new RecordTransactionBuilder();
        for (RecordRef recordRef : selection) {
            tx.delete(recordRef.getFormId(), recordRef.getRecordId());
        }

        return formStore.updateRecords(tx.build());
    }

    @Override
    public void onComplete() {
    }
}
