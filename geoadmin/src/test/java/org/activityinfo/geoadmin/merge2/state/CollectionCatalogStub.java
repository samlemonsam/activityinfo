package org.activityinfo.geoadmin.merge2.state;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.ResourceCollection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CollectionCatalogStub implements CollectionCatalog {
    
    private Map<ResourceId, JsonResourceCollection> map = new HashMap<>();
    
    
    @Override
    public Optional<ResourceCollection> getCollection(ResourceId resourceId) {
        return Optional.<ResourceCollection>fromNullable(map.get(resourceId));
    }

    @Override
    public Optional<ResourceCollection> lookupCollection(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return getCollection(resourceId).get().getFormClass();
    }
    
    public void addJsonCollection(String resourceName) throws IOException {
        add(new JsonResourceCollection(resourceName));
    }
    
    public void add(JsonResourceCollection accessor) {
        map.put(accessor.getFormClass().getId(), accessor);
    }

    public boolean contains(ResourceId resourceId) {
        return map.containsKey(resourceId);
    }
    
}
