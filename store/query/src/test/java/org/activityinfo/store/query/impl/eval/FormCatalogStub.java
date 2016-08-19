package org.activityinfo.store.query.impl.eval;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FormCatalogStub implements FormCatalog {

    private Map<ResourceId, AccessorStub> formMap = new HashMap<>();


    public AccessorStub addForm(FormClass formClass) {
        AccessorStub accessorStub = new AccessorStub(formClass);
        formMap.put(formClass.getId(), accessorStub);
        return accessorStub;
    }

    public FormTree getTree(ResourceId resourceId) {
        FormTreeBuilder builder = new FormTreeBuilder(this);
        return builder.queryTree(resourceId);
    }

    @Override
    public Optional<FormAccessor> getForm(ResourceId formId) {
        return Optional.<FormAccessor>fromNullable(formMap.get(formId));
    }

    @Override
    public Optional<FormAccessor> lookupForm(ResourceId recordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CatalogEntry> getRootEntries() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        AccessorStub form = formMap.get(resourceId);
        if(form == null) {
            throw new IllegalArgumentException();
        }
        return form.getFormClass();
    }

    public class AccessorStub implements FormAccessor {

        private FormClass formClass;
        private int numRows = 10;

        public AccessorStub(FormClass formClass) {
            this.formClass = formClass;
        }

        public AccessorStub withRowCount(int numRows) {
            this.numRows = numRows;
            return this;
        }

        @Override
        public FormPermissions getPermissions(int userId) {
            return FormPermissions.full();
        }

        @Override
        public Optional<FormRecord> get(ResourceId resourceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RecordVersion> getVersions(ResourceId recordId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RecordVersion> getVersionsForParent(ResourceId parentRecordId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FormClass getFormClass() {
            return formClass;
        }

        @Override
        public void updateFormClass(FormClass formClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(RecordUpdate update) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(RecordUpdate update) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ColumnQueryBuilder newColumnQuery() {
            return new QueryBuilderStub(this);
        }

        @Override
        public long cacheVersion() {
            return 0;
        }
    }

    private class QueryBuilderStub implements ColumnQueryBuilder {

        private List<CursorObserver<ResourceId>> idObservers = Lists.newArrayList();
        private AccessorStub collection;

        public QueryBuilderStub(AccessorStub collection) {
            this.collection = collection;
        }


        @Override
        public void only(ResourceId resourceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addResourceId(CursorObserver<ResourceId> observer) {
            idObservers.add(observer);
        }

        @Override
        public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void execute() {
            for (int i = 0; i < collection.numRows; i++) {
                for (CursorObserver<ResourceId> idObserver : idObservers) {
                    idObserver.onNext(ResourceId.valueOf(collection.formClass.getId() + "_R" + i));
                }
            }
            for (CursorObserver<ResourceId> idObserver : idObservers) {
                idObserver.done();
            }
        }
    }
}
