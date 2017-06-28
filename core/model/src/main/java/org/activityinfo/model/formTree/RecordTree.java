package org.activityinfo.model.formTree;

import com.google.common.base.Function;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Maybe;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * A FormRecord, it's FormTree, and all related records
 */
public class RecordTree {

    private final FormTree formTree;
    private final RecordRef rootRecordRef;
    private FormInstance root;

    private final Map<RecordRef, Maybe<FormInstance>> relatedRecords;

    public RecordTree(FormTree formTree,
                      RecordRef rootRecordRef,
                      Map<RecordRef, Maybe<FormInstance>> records) {
        this.formTree = formTree;
        this.rootRecordRef = rootRecordRef;
        this.relatedRecords = records;
        this.root = records.get(rootRecordRef).get();
    }

    public FormInstance getRoot() {
        return root;
    }

    public Maybe<FormInstance> getRecord(RecordRef ref) {
        Maybe<FormInstance> record = this.relatedRecords.get(ref);
        if(record == null) {
            return Maybe.notFound();
        }
        return record;
    }

    public Maybe<String> composeLabel(RecordRef recordRef) {
        return getRecord(recordRef).transform(new Function<FormInstance, String>() {
            @Override
            public String apply(FormInstance record) {
                FormClass formClass = formTree.getFormClass(record.getFormId());


                return null;
            }
        });
    }

    public FormTree getFormTree() {
        return formTree;
    }
}
