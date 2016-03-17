package org.activityinfo.store.mysql.mapping;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.SQLException;

/**
 * Provides a Mapping
 */
public interface SimpleTable {

    boolean accept(ResourceId formClassId);

    TableMapping getMapping(QueryExecutor executor, ResourceId formClassId) throws SQLException;

    /**
     * Map a resourceId to it's collection
     *
     * @param queryExecutor
     * @param id the id of the resource
     * @return the id of the collection
     */
    Optional<ResourceId> lookupCollection(QueryExecutor queryExecutor, ResourceId id) throws SQLException;
    
}
