package org.activityinfo.model.formTree;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Maybe;

import java.util.List;
import java.util.Map;

/**
 * A FormRecord, it's FormTree, and all related records
 */
public class RecordTree {

    public static class ParentKey {
        private final RecordRef parentRef;
        private final ResourceId subFormId;

        public ParentKey(RecordRef parentRef, ResourceId subFormId) {
            this.parentRef = parentRef;
            this.subFormId = subFormId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ParentKey parentKey = (ParentKey) o;

            if (!parentRef.equals(parentKey.parentRef)) return false;
            return subFormId.equals(parentKey.subFormId);

        }

        @Override
        public int hashCode() {
            int result = parentRef.hashCode();
            result = 31 * result + subFormId.hashCode();
            return result;
        }
    }

    private final FormTree formTree;
    private final FormInstance root;

    private final Map<RecordRef, Maybe<FormInstance>> relatedRecords;
    private final Multimap<ParentKey, FormInstance> subRecords;

    public RecordTree(FormTree formTree,
                      RecordRef rootRecordRef,
                      Map<RecordRef, Maybe<FormInstance>> records, Multimap<ParentKey, FormInstance> subRecords) {
        this.formTree = formTree;
        this.relatedRecords = records;
        this.subRecords = subRecords;
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

    public FormTree getFormTree() {
        return formTree;
    }

    public Iterable<FormInstance> getSubRecords(RecordRef parentRef, ResourceId formId) {
        return subRecords.get(new ParentKey(parentRef, formId));
    }
}
