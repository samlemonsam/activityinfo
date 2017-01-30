package org.activityinfo.geoadmin.merge2.state;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.FormCatalog;
import org.activityinfo.service.store.FormStorage;

import java.io.IOException;
import java.util.*;

public class FormCatalogStub implements FormCatalog {
    
    private Map<ResourceId, JsonFormStorage> map = new HashMap<>();
    
    
    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {
        return Optional.<FormStorage>fromNullable(map.get(formId));
    }

    @Override
    public Optional<FormStorage> lookupForm(ResourceId recordId) {
        throw new UnsupportedOperationException();
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
        return null;
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return getForm(resourceId).get().getFormClass();
    }
    
    public void addJsonCollection(String resourceName) throws IOException {
        add(new JsonFormStorage(resourceName));
    }
    
    public void add(JsonFormStorage accessor) {
        map.put(accessor.getFormClass().getId(), accessor);
    }

    public boolean contains(ResourceId resourceId) {
        return map.containsKey(resourceId);
    }
    
}
