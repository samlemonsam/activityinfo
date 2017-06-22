package org.activityinfo.store.query.shared;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.spi.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FormCatalogStub implements FormCatalog {

    private Map<ResourceId, StorageStub> formMap = new HashMap<>();


    public StorageStub addForm(FormClass formClass) {
        StorageStub accessorStub = new StorageStub(formClass);
        formMap.put(formClass.getId(), accessorStub);
        return accessorStub;
    }

    public FormTree getTree(ResourceId resourceId) {
        FormTreeBuilder builder = new FormTreeBuilder(this);
        return builder.queryTree(resourceId);
    }

    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {
        return Optional.<FormStorage>fromNullable(formMap.get(formId));
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
    public List<CatalogEntry> getChildren(String parentId, int userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        StorageStub form = formMap.get(resourceId);
        if(form == null) {
            throw new IllegalArgumentException();
        }
        return form.getFormClass();
    }

    public class StorageStub implements FormStorage {

        private FormClass formClass;
        private int numRows = 10;

        public StorageStub(FormClass formClass) {
            this.formClass = formClass;
        }

        public StorageStub withRowCount(int numRows) {
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

        @Override
        public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value) {
            throw new UnsupportedOperationException();
        }
    }

    private class QueryBuilderStub implements ColumnQueryBuilder {

        private List<CursorObserver<ResourceId>> idObservers = Lists.newArrayList();
        private StorageStub collection;

        public QueryBuilderStub(StorageStub collection) {
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
