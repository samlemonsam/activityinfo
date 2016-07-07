package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.hrd.HrdCatalog;
import org.activityinfo.store.mysql.collections.CollectionProvider;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HrdProvider implements CollectionProvider {
    
    private HrdCatalog catalog = new HrdCatalog();
    
    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == 'c';
    }

    @Override
    public ResourceCollection openCollection(QueryExecutor executor, ResourceId formClassId) throws SQLException {
        Optional<ResourceCollection> collection = catalog.getCollection(formClassId);
        if(!collection.isPresent()) {
            throw new RuntimeException("No such collection");
        }
        return collection.get();
    }

    @Override
    public Optional<ResourceId> lookupCollection(QueryExecutor executor, ResourceId resourceId) throws SQLException {
        Optional<ResourceCollection> collection = catalog.lookupCollection(resourceId);
        if(collection.isPresent()) {
            return Optional.of(collection.get().getFormClass().getId());
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Map<ResourceId, ResourceCollection> openCollections(QueryExecutor executor, Set<ResourceId> resourceIds) throws SQLException {
        Map<ResourceId, ResourceCollection> map = new HashMap<>();
        for (ResourceId resourceId : resourceIds) {
            if(accept(resourceId)) {
                map.put(resourceId, openCollection(executor, resourceId));
            }
        }
        return map;
    }
}
