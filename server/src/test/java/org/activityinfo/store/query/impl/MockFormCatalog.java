package org.activityinfo.store.query.impl;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class MockFormCatalog implements FormCatalog {
    
    private static final ResourceId COLLECTION_ID = ResourceId.valueOf("XYZ123");
    
    private MockFormAccessor collection = new MockFormAccessor();
    
    
    @Override
    public Optional<FormAccessor> getForm(ResourceId formId) {
        if (formId == null || formId.asString().equalsIgnoreCase("foobar")) {
            return Optional.absent();
        }
        return Optional.<FormAccessor>of(collection);
    }

    @Override
    public Optional<FormAccessor> lookupForm(ResourceId recordId) {
        return Optional.absent();
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CatalogEntry> getRootEntries() {
        return Collections.emptyList();
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId, int userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        
        FormClass formClass = new FormClass(COLLECTION_ID);
        
        return formClass;
    }
    
    private class MockFormAccessor implements FormAccessor {

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
            return Collections.emptyList();
        }

        @Override
        public List<RecordVersion> getVersionsForParent(ResourceId parentRecordId) {
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
        public void update(RecordUpdate update) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(RecordUpdate update) {
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

    }
}
