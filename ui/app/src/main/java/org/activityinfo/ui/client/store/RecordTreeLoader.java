package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableTree;
import org.activityinfo.promise.Maybe;

import javax.annotation.Nonnull;
import java.util.*;

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
            throw new UnsupportedOperationException();
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
    }

    public abstract class Node {

        protected abstract Iterable<NodeKey> getChildren();

        protected abstract void addTo(Map<RecordRef, Maybe<FormInstance>> records);
    }

    private class RecordNode extends Node {
        private final RecordRef ref;
        private final Maybe<FormInstance> record;

        public RecordNode(RecordRef ref, Maybe<FormRecord> record) {
            this.ref = ref;
            this.record = record.transform(r -> {
                FormClass formClass = formTree.getFormClass(ResourceId.valueOf(r.getFormId()));
                return FormInstance.toFormInstance(formClass, r);
            });
        }

        @Override
        public Iterable<NodeKey> getChildren() {
            if(record.isVisible()) {
                return getChildren(record.get());
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        protected void addTo(Map<RecordRef, Maybe<FormInstance>> records) {
            records.put(ref, record);
        }

        private Iterable<NodeKey> getChildren(FormInstance record) {
            Set<NodeKey> children = new HashSet<>();

            for (FieldValue value : record.getFieldValueMap().values()) {
                if(value instanceof ReferenceValue) {
                    for (RecordRef recordRef : ((ReferenceValue) value).getReferences()) {
                        children.add(new RecordKey(recordRef));
                    }
                }
            }
            return children;
        }
    }

    private class SubFormNode extends Node {
        private List<FormInstance> records;

        @Override
        public Iterable<NodeKey> getChildren() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void addTo(Map<RecordRef, Maybe<FormInstance>> records) {
            throw new UnsupportedOperationException();
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

        for (Observable<Node> node : nodes.values()) {
            node.get().addTo(records);
        }
        return new RecordTree(formTree, rootRecordRef, records);
    }

    @Override
    public String toString() {
        return "RecordTree{" + rootRecordRef + "}";
    }
}
