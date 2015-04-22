package org.activityinfo.geoadmin.source;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionAccessor;
import org.activityinfo.service.store.CollectionCatalog;
import org.geotools.data.shapefile.ShapefileDataStore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class FeatureSourceCatalog implements CollectionCatalog {
    
    private Map<ResourceId, FeatureSourceAccessor> sources = new HashMap<>();
    
    
    public void add(ResourceId id, String path) throws IOException {
        File shapeFile = new File(path);
        ShapefileDataStore dataStore = new ShapefileDataStore(shapeFile.toURI().toURL());
        sources.put(id, new FeatureSourceAccessor(id, dataStore.getFeatureSource()));
    }
    
    @Override
    public CollectionAccessor getCollection(ResourceId resourceId) {
        FeatureSourceAccessor accessor = sources.get(resourceId);
        if(accessor == null) {
            throw new IllegalArgumentException(resourceId.asString());
        }
        return accessor;
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return getCollection(resourceId).getFormClass();
    }
}
