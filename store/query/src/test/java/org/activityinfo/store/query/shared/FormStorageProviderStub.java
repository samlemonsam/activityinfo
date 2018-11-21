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
package org.activityinfo.store.query.shared;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
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

class FormStorageProviderStub implements FormStorageProvider {

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
    public FormClass getFormClass(ResourceId formId) {
        StorageStub form = formMap.get(formId);
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
        public Optional<FormRecord> get(ResourceId resourceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<FormRecord> getSubRecords(ResourceId resourceId) {
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
        public void add(TypedRecordUpdate update) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(TypedRecordUpdate update) {
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
