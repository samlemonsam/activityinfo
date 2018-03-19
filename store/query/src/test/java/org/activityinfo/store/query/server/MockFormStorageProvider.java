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
package org.activityinfo.store.query.server;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public class MockFormStorageProvider implements FormStorageProvider, TransactionalStorageProvider {
    
    private static final ResourceId COLLECTION_ID = ResourceId.valueOf("XYZ123");
    
    private MockFormStorage collection = new MockFormStorage();
    
    
    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {
        if (formId == null || formId.asString().equalsIgnoreCase("foobar")) {
            return Optional.absent();
        }
        return Optional.<FormStorage>of(collection);
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass(ResourceId formId) {
        
        FormClass formClass = new FormClass(COLLECTION_ID);
        
        return formClass;
    }

    @Override
    public void begin() {

    }

    @Override
    public void commit() {
    }

    @Override
    public void rollback() {
    }

    private class MockFormStorage implements FormStorage {

        @Override
        public FormPermissions getPermissions(int userId) {
            return FormPermissions.readWrite();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateFormClass(FormClass formClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(TypedRecordUpdate update) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(TypedRecordUpdate update) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ColumnQueryBuilder newColumnQuery() {
            throw new UnsupportedOperationException();
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
}
