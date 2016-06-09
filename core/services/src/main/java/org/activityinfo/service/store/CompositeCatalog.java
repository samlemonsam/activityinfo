package org.activityinfo.service.store;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CompositeCatalog implements CollectionCatalog {

    private List<CollectionCatalog> catalogs;
    
    public CompositeCatalog(CollectionCatalog... catalogs) {
        this.catalogs = Arrays.asList(catalogs);
    }


    @Override
    public Optional<ResourceCollection> getCollection(ResourceId collectionId) {
        for (CollectionCatalog catalog : catalogs) {
            Optional<ResourceCollection> collection = catalog.getCollection(collectionId);
            if(collection.isPresent()) {
                return collection;
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<ResourceCollection> lookupCollection(ResourceId resourceId) {
        for (CollectionCatalog catalog : catalogs) {
            Optional<ResourceCollection> collection = catalog.lookupCollection(resourceId);
            if(collection.isPresent()) {
                return collection;
            }
        }
        return Optional.absent();
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> collectionIds) {
        Map<ResourceId, FormClass> map = Maps.newHashMap();
        for (CollectionCatalog catalog : catalogs) {
            map.putAll(catalog.getFormClasses(collectionIds));
        }
        return map;
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        for (CollectionCatalog catalog : catalogs) {
            Optional<ResourceCollection> collection = catalog.getCollection(resourceId);
            if(collection.isPresent()) {
                return collection.get().getFormClass();
            }
        }
        throw new IllegalArgumentException("No such form class: " + resourceId);
    }
}
