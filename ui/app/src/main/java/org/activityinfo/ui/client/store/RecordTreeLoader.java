package org.activityinfo.ui.client.store;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.model.form.*;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableTree;
import org.activityinfo.promise.Maybe;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class RecordTreeLoader implements ObservableTree.TreeLoader<
    RecordTreeLoader.NodeKey,
    RecordTreeLoader.Node,
    RecordTree> {

    public abstract class NodeKey {
        public abstract Observable<Node> get(FormStore formStore);
    }

    private class RecordKey extends NodeKey {
        @Nonnull
        private final RecordRef ref;

        public RecordKey(@Nonnull RecordRef ref) {
            this.ref = ref;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RecordKey recordKey = (RecordKey) o;

            return ref.equals(recordKey.ref);
        }

        @Override
        public int hashCode() {
            return ref.hashCode();
        }

        @Override
        public String toString() {
            return "RecordKey{" + ref + "}";
        }

        @Override
        public Observable<Node> get(FormStore formStore) {
            return formStore.getRecord(ref).transform(result -> new RecordNode(ref, result));
        }
    }

    private class SubFormKey extends NodeKey {
        @Nonnull
        private final RecordRef parentRecordId;

        @Nonnull
        private final ResourceId formId;

        public SubFormKey(@Nonnull RecordRef parentRecordId, @Nonnull ResourceId formId) {
            this.parentRecordId = parentRecordId;
            this.formId = formId;
        }

        @Override
        public Observable<Node> get(FormStore formStore) {
            return formStore
                .getSubRecords(formId, parentRecordId)
                .transform(list -> new SubFormNode(parentRecordId, formId, list));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SubFormKey that = (SubFormKey) o;

            if (!parentRecordId.equals(that.parentRecordId)) return false;
            return formId.equals(that.formId);

        }

        @Override
        public int hashCode() {
            int result = parentRecordId.hashCode();
            result = 31 * result + formId.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "SubFormKey{" +
                "parent=" + parentRecordId +
                ", formId=" + formId +
                '}';
        }
    }

    public abstract class Node {

        protected abstract Iterable<NodeKey> getChildren();

        protected abstract void addTo(Map<RecordRef, Maybe<FormInstance>> records, Multimap<RecordTree.ParentKey, FormInstance> subRecords);
    }

    private class RecordNode extends Node {
        private final RecordRef ref;
        private final FormClass formClass;
        private final Maybe<FormInstance> record;

        public RecordNode(RecordRef ref, Maybe<FormRecord> record) {
            this.ref = ref;
            this.formClass = formTree.getFormClass(ref.getFormId());
            this.record = record.transform(r -> FormInstance.toFormInstance(formClass, r));
        }

        @Override
        public Iterable<NodeKey> getChildren() {
            Set<NodeKey> children = new HashSet<>();
            if(record.isVisible()) {
                findChildren(children, formClass, record.get());
            }
            return children;
        }

        @Override
        protected void addTo(Map<RecordRef, Maybe<FormInstance>> records, Multimap<RecordTree.ParentKey, FormInstance> subRecords) {
            records.put(ref, record);
        }

    }

    private class SubFormNode extends Node {
        private final RecordTree.ParentKey parentKey;
        private final FormClass formClass;
        private final List<FormInstance> records;

        public SubFormNode(RecordRef parentRef, ResourceId formId, List<FormRecord> records) {
            this.parentKey = new RecordTree.ParentKey(parentRef, formId);
            formClass = formTree.getFormClass(formId);
            this.records = records
                .stream()
                .map(record -> FormInstance.toFormInstance(formClass, record))
                .collect(Collectors.toList());
        }

        @Override
        public Iterable<NodeKey> getChildren() {
            Set<NodeKey> children = new HashSet<>();
            for (FormInstance record : records) {
                findChildren(children, formClass, record);
            }
            return children;
        }

        @Override
        protected void addTo(Map<RecordRef, Maybe<FormInstance>> records, Multimap<RecordTree.ParentKey, FormInstance> subRecords) {
            subRecords.putAll(parentKey, this.records);
            for (FormInstance record : this.records) {
                records.put(record.getRef(), Maybe.of(record));
            }
        }
    }


    private void findChildren(Set<NodeKey> children, FormClass schema, FormInstance record) {
        // Add referenced records
        for (FieldValue value : record.getFieldValueMap().values()) {
            if (value instanceof ReferenceValue) {
                for (RecordRef recordRef : ((ReferenceValue) value).getReferences()) {
                    children.add(new RecordKey(recordRef));
                }
            }
        }
        // Add sub forms
        for (FormField formField : schema.getFields()) {
            if (formField.getType() instanceof SubFormReferenceType) {
                SubFormReferenceType subFormType = (SubFormReferenceType) formField.getType();
                children.add(new SubFormKey(record.getRef(), subFormType.getClassId()));
            }
        }
    }


    private final FormStore formStore;
    private final FormTree formTree;
    private final RecordRef rootRecordRef;

    public RecordTreeLoader(FormStore formStore, FormTree formTree, RecordRef rootRecordRef) {
        this.formStore = formStore;
        this.formTree = formTree;
        this.rootRecordRef = rootRecordRef;
    }

    @Override
    public NodeKey getRootKey() {
        return new RecordKey(rootRecordRef);
    }

    @Override
    public Observable<Node> get(NodeKey nodeKey) {
        return nodeKey.get(formStore);
    }

    @Override
    public Iterable<NodeKey> getChildren(Node node) {
        return node.getChildren();
    }

    @Override
    public RecordTree build(Map<NodeKey, Observable<Node>> nodes) {

        Map<RecordRef, Maybe<FormInstance>> records = new HashMap<>();
        Multimap<RecordTree.ParentKey, FormInstance> subRecords = HashMultimap.create();

        for (Observable<Node> node : nodes.values()) {
            node.get().addTo(records, subRecords);
        }
        return new RecordTree(formTree, rootRecordRef, records, subRecords);
    }

    @Override
    public String toString() {
        return "RecordTree{" + rootRecordRef + "}";
    }
}
