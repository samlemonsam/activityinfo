package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.MappingProvider;

import java.sql.SQLException;


public class SimpleTableCollectionProvider implements CollectionProvider {
    
    private final MappingProvider mappingProvider;

    public SimpleTableCollectionProvider(MappingProvider mappingProvider) {
        this.mappingProvider = mappingProvider;
    }

    @Override
    public boolean accept(ResourceId formClassId) {
        return mappingProvider.accept(formClassId);
    }

    @Override
    public ResourceCollection getAccessor(QueryExecutor executor, ResourceId formClassId) throws SQLException {
        return new SimpleTableCollection(mappingProvider.getMapping(executor, formClassId), executor);
    }
}
