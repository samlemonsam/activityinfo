package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class LocationCollectionProvider implements CollectionProvider {


    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.LOCATION_TYPE_DOMAIN;
    }

    @Override
    public ResourceCollection openCollection(QueryExecutor executor, ResourceId formClassId) throws SQLException {
        return new LocationCollection(executor, formClassId);
    }

    @Override
    public Optional<ResourceId> lookupCollection(QueryExecutor queryExecutor, ResourceId id) throws SQLException {
        return Optional.absent();
    }

    @Override
    public Map<ResourceId, ResourceCollection> openCollections(QueryExecutor executor, Set<ResourceId> collectionIds) throws SQLException {
        Map<ResourceId, ResourceCollection> result = new HashMap<>();
        for (ResourceId collectionId : collectionIds) {
            if(accept(collectionId)) {
                result.put(collectionId, openCollection(executor, collectionId));
            }
        }
        return result;
    }

}
