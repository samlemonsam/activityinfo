package org.activityinfo.geoadmin.merge2.state;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.service.store.FormCatalog;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FormCatalogStub implements FormCatalog {
    
    private Map<ResourceId, JsonFormAccessor> map = new HashMap<>();
    
    
    @Override
    public Optional<FormAccessor> getForm(ResourceId formId) {
        return Optional.<FormAccessor>fromNullable(map.get(formId));
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
    public FormClass getFormClass(ResourceId resourceId) {
        return getForm(resourceId).get().getFormClass();
    }
    
    public void addJsonCollection(String resourceName) throws IOException {
        add(new JsonFormAccessor(resourceName));
    }
    
    public void add(JsonFormAccessor accessor) {
        map.put(accessor.getFormClass().getId(), accessor);
    }

    public boolean contains(ResourceId resourceId) {
        return map.containsKey(resourceId);
    }
    
}
