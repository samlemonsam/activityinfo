package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionAccessor;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.TableMapping;

import java.sql.SQLException;

public interface CollectionProvider {

    boolean accept(ResourceId formClassId);

    CollectionAccessor getAccessor(QueryExecutor executor, ResourceId formClassId) throws SQLException;
}
