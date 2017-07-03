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

    public FormStructure(FormTree formTree) {
        this.formTree = formTree;
        this.existingRecord = Maybe.notFound();
    }

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
