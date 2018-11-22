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
package org.activityinfo.model.formTree;

import com.google.common.collect.Multimap;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Maybe;

import java.util.ArrayList;
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
    private final TypedFormRecord root;

    private final Map<RecordRef, Maybe<TypedFormRecord>> relatedRecords;
    private final Multimap<ParentKey, TypedFormRecord> subRecords;

    public RecordTree(FormTree formTree,
                      RecordRef rootRecordRef,
                      Map<RecordRef, Maybe<TypedFormRecord>> records, Multimap<ParentKey, TypedFormRecord> subRecords) {
        this.formTree = formTree;
        this.relatedRecords = records;
        this.subRecords = subRecords;
        this.root = records.get(rootRecordRef).get();
    }

    public TypedFormRecord getRoot() {
        return root;
    }

    public RecordRef getRootRef() {
        return root.getRef();
    }

    public Maybe<TypedFormRecord> getRecord(RecordRef ref) {
        Maybe<TypedFormRecord> record = this.relatedRecords.get(ref);
        if(record == null) {
            return Maybe.notFound();
        }
        return record;
    }

    public boolean contains(RecordRef ref) {
        return this.relatedRecords.containsKey(ref);
    }

    public FormTree getFormTree() {
        return formTree;
    }

    public Iterable<TypedFormRecord> getSubRecords(RecordRef parentRef, ResourceId formId) {
        return subRecords.get(new ParentKey(parentRef, formId));
    }

    public Iterable<TypedFormRecord> getSubRecords(ResourceId subFormId) {
        return getSubRecords(root.getRef(), subFormId);
    }

    public RecordTree subTree(RecordRef ref) {
        return new RecordTree(formTree.subTree(ref.getFormId()), ref, this.relatedRecords, this.subRecords);
    }

    public List<RecordTree> buildSubTrees(RecordRef parentRef, FormTree subTree) {

        List<RecordTree> recordTrees = new ArrayList<>();
        Iterable<TypedFormRecord> subRecords = getSubRecords(parentRef, subTree.getRootFormId());

        for (TypedFormRecord subRecord : subRecords) {
            recordTrees.add(new RecordTree(subTree, subRecord.getRef(), this.relatedRecords, this.subRecords));
        }
        return recordTrees;
    }

}
