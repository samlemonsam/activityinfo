/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
