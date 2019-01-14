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
package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;
import org.activityinfo.ui.client.store.FormStore;

/**
 * All the existing data required to layout the data form.
 *
 */
public class FormStructure {

    private FormTree formTree;
    private Maybe<RecordTree> existingRecord;

    public FormStructure(FormTree formTree, Maybe<RecordTree> existingRecord) {
        this.formTree = formTree;
        this.existingRecord = existingRecord;
    }

    public FormTree getFormTree() {
        return formTree;
    }

    public Maybe<RecordTree> getExistingRecord() {
        return existingRecord;
    }

    public static Observable<FormStructure> fetch(FormStore store, RecordRef ref) {
        Observable<FormTree> formTree = store.getFormTree(ref.getFormId());
        Observable<Maybe<RecordTree>> existingRecord = store.getRecordTree(ref);

        return Observable.transform(formTree, existingRecord, FormStructure::new);
    }
}
