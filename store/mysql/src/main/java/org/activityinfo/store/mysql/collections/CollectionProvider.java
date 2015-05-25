package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.SQLException;

public interface CollectionProvider {

    boolean accept(ResourceId formClassId);

    ResourceCollection getAccessor(QueryExecutor executor, ResourceId formClassId) throws SQLException;
}
