package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.MappingProvider;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;

import java.sql.SQLException;

public class ProjectCollection implements MappingProvider {
    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.PROJECT_CLASS_DOMAIN;
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formClassId) throws SQLException {
        int databaseId = CuidAdapter.getLegacyIdFromCuid(formClassId);
        TableMappingBuilder mapping = TableMappingBuilder.newMapping(formClassId, "project");
        throw new UnsupportedOperationException();
    }
}
