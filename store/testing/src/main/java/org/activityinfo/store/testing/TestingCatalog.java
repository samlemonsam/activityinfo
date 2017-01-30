package org.activityinfo.store.testing;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.service.store.FormCatalog;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public class TestingCatalog implements FormCatalog {
    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return null;
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        return null;
    }

    @Override
    public Optional<FormAccessor> getForm(ResourceId formId) {
        return null;
    }

    @Override
    public Optional<FormAccessor> lookupForm(ResourceId recordId) {
        return null;
    }

    @Override
    public List<CatalogEntry> getRootEntries() {
        return null;
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId, int userId) {
        return null;
    }
}
