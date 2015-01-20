package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionAccessor;
import org.activityinfo.store.mysql.mapping.MappingProvider;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.*;

import java.sql.SQLException;
import java.util.logging.Logger;


public class SiteCollectionProvider implements CollectionProvider {
    
    private static final Logger LOGGER = Logger.getLogger(SiteCollectionProvider.class.getName());


    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.ACTIVITY_DOMAIN;
    }

    @Override
    public CollectionAccessor getAccessor(QueryExecutor executor, ResourceId formClassId) throws SQLException {
        
        Activity activity = Activity.query(executor, CuidAdapter.getLegacyIdFromCuid(formClassId));
        ActivityTableMappingBuilder mapping = ActivityTableMappingBuilder.site(activity);

        return new SiteCollection(activity, mapping.build(), executor);
    }
}
