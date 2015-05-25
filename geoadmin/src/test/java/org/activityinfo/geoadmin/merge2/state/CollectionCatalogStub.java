package org.activityinfo.geoadmin.merge2.state;

import com.google.common.base.Preconditions;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionAccessor;
import org.activityinfo.service.store.CollectionCatalog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CollectionCatalogStub implements CollectionCatalog {
    
    private Map<ResourceId, JsonCollectionAccessor> map = new HashMap<>();
    
    
    @Override
    public CollectionAccessor getCollection(ResourceId resourceId) {
        return Preconditions.checkNotNull(map.get(resourceId));
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return getCollection(resourceId).getFormClass();
    }
    
    public void addJsonCollection(String resourceName) throws IOException {
        add(new JsonCollectionAccessor(resourceName));
    }
    
    public void add(JsonCollectionAccessor accessor) {
        map.put(accessor.getFormClass().getId(), accessor);
    }

    public boolean contains(ResourceId resourceId) {
        return map.containsKey(resourceId);
    }
    
}
