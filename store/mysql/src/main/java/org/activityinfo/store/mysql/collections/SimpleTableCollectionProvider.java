package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.MappingProvider;

import java.sql.SQLException;


public class SimpleTableCollectionProvider implements CollectionProvider {
    
    private final MappingProvider mappingProvider;
    private Authorizer authorizer;

    public SimpleTableCollectionProvider(MappingProvider mappingProvider, CollectionPermissions permissions) {
        this.mappingProvider = mappingProvider;
        this.authorizer = new ConstantAuthorizer(permissions);
    }

    @Override
    public boolean accept(ResourceId formClassId) {
        return mappingProvider.accept(formClassId);
    }

    @Override
    public ResourceCollection getAccessor(QueryExecutor executor, ResourceId formClassId) throws SQLException {
        return new SimpleTableCollection(mappingProvider.getMapping(executor, formClassId), authorizer, executor);
    }

    @Override
    public Optional<ResourceId> lookupCollection(QueryExecutor executor, ResourceId resourceId) throws SQLException {
        return Optional.absent();
    }

}
