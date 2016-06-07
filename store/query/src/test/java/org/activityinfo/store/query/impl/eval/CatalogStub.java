package org.activityinfo.store.query.impl.eval;

import com.google.appengine.repackaged.com.google.api.client.util.Lists;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 6-4-16.
 */
class CatalogStub implements CollectionCatalog {

    private Map<ResourceId, CollectionStub> collectionMap = new HashMap<>();


    public CollectionStub addForm(FormClass formClass) {
        CollectionStub collectionStub = new CollectionStub(formClass);
        collectionMap.put(formClass.getId(), collectionStub);
        return collectionStub;
    }

    public FormTree getTree(ResourceId resourceId) {
        FormTreeBuilder builder = new FormTreeBuilder(this);
        return builder.queryTree(resourceId);
    }

    @Override
    public Optional<ResourceCollection> getCollection(ResourceId collectionId) {
        return Optional.<ResourceCollection>fromNullable(collectionMap.get(collectionId));
    }

    @Override
    public Optional<ResourceCollection> lookupCollection(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> collectionIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        CollectionStub collection = collectionMap.get(resourceId);
        if(collection == null) {
            throw new IllegalArgumentException();
        }
        return collection.getFormClass();
    }

    public class CollectionStub implements ResourceCollection {

        private FormClass formClass;
        private int numRows = 10;

        public CollectionStub(FormClass formClass) {
            this.formClass = formClass;
        }

        public CollectionStub withRowCount(int numRows) {
            this.numRows = numRows;
            return this;
        }

        @Override
        public CollectionPermissions getPermissions(int userId) {
            return CollectionPermissions.full();
        }

        @Override
        public Optional<Resource> get(ResourceId resourceId) {
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
        public void add(ResourceUpdate update) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(ResourceUpdate update) {
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
        private CollectionStub collection;

        public QueryBuilderStub(CollectionStub collection) {
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
        public void execute() throws IOException {
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
