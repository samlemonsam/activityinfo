package org.activityinfo.geoadmin.source;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.query.RowSource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.ResourceCollection;
import org.geotools.data.shapefile.ShapefileDataStore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class FeatureSourceCatalog implements CollectionCatalog {

    public static final String FILE_PREFIX = "file://";
    private Map<ResourceId, FeatureSourceCollection> sources = new HashMap<>();
    
    public boolean isLocalResource(ResourceId resourceId) {
        return resourceId.asString().startsWith(FILE_PREFIX);
    }

    public boolean isLocalQuery(QueryModel queryModel) {
        for (RowSource rowSource : queryModel.getRowSources()) {
            if(!isLocalResource(rowSource.getRootFormClass())) {
                return false;
            }
        }
        return true;
    }
    
    public void add(ResourceId id, String path) throws IOException {
        File shapeFile = new File(path);
        ShapefileDataStore dataStore = new ShapefileDataStore(shapeFile.toURI().toURL());
        sources.put(id, new FeatureSourceCollection(id, dataStore.getFeatureSource()));
    }
    
    @Override
    public ResourceCollection getCollection(ResourceId resourceId) {

        FeatureSourceCollection accessor = sources.get(resourceId);
        if(accessor == null) {

            Preconditions.checkArgument(resourceId.asString().startsWith(FILE_PREFIX),
                    "FeatureSourceCatalog supports only resourceIds starting with file://");

            try {
                File shapeFile = new File(resourceId.asString().substring(FILE_PREFIX.length()));
                ShapefileDataStore dataStore = new ShapefileDataStore(shapeFile.toURI().toURL());
                accessor = new FeatureSourceCollection(resourceId, dataStore.getFeatureSource());
                sources.put(resourceId, accessor);

            } catch (Exception e) {
                throw new IllegalArgumentException("Could not load " + resourceId, e);
            }
        }
        return accessor;
    }

    @Override
    public Optional<ResourceCollection> lookupCollection(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return getCollection(resourceId).getFormClass();
    }


}
