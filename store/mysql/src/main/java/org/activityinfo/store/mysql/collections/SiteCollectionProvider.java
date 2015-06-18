package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.ActivityTableMappingBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;


public class SiteCollectionProvider implements CollectionProvider {
    
    private static final Logger LOGGER = Logger.getLogger(SiteCollectionProvider.class.getName());


    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.ACTIVITY_DOMAIN;
    }

    @Override
    public ResourceCollection getAccessor(QueryExecutor executor, ResourceId formClassId) throws SQLException {
        
        Activity activity = Activity.query(executor, CuidAdapter.getLegacyIdFromCuid(formClassId));
        ActivityTableMappingBuilder mapping = ActivityTableMappingBuilder.site(activity);

        return new SiteCollection(activity, mapping.build(), executor);
    }

    @Override
    public Optional<ResourceId> lookupCollection(QueryExecutor executor, ResourceId resourceId) throws SQLException {
        if(resourceId.getDomain() == CuidAdapter.SITE_DOMAIN) {
            int siteId = CuidAdapter.getLegacyIdFromCuid(resourceId);
            try(ResultSet rs = executor.query("SELECT activityId FROM site where siteId = " + siteId)) {
                if(rs.next()) {
                    ResourceId activityId = CuidAdapter.activityFormClass(rs.getInt(1));   
                    return Optional.of(activityId);
                }
            }
        }
        return Optional.absent();
    }


}
