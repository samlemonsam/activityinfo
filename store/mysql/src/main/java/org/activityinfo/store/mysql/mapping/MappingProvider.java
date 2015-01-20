package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionAccessor;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.TableMapping;

import java.sql.SQLException;

/**
 * Provides a Mapping
 */
public interface MappingProvider {

    boolean accept(ResourceId formClassId);

    TableMapping getMapping(QueryExecutor executor, ResourceId formClassId) throws SQLException;

}
