package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

/**
 * Provides
 */
public interface MappingProvider {

    boolean accept(ResourceId formClassId);

    TableMapping getMapping(QueryExecutor executor, ResourceId formClassId);

}
