package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.FormNotFoundException;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.ActivityTableMappingBuilder;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityLoader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Collections.singleton;


public class ActivityCollectionProvider implements CollectionProvider {
    
    private static final Logger LOGGER = Logger.getLogger(ActivityCollectionProvider.class.getName());

    /**
     * Caches activities within a session to avoid fetching Activity for both Site and ReportingFrequency table
     */
    private ActivityLoader activityLoader;

    public ActivityCollectionProvider(ActivityLoader activityLoader) {
        this.activityLoader = activityLoader;
    }

    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.ACTIVITY_DOMAIN ||
               formClassId.getDomain() == CuidAdapter.MONTHLY_REPORT_FORM_CLASS;
    }

    @Override
    public ResourceCollection openCollection(QueryExecutor executor, ResourceId formClassId) throws SQLException {
        int activityId = CuidAdapter.getLegacyIdFromCuid(formClassId);
        Map<Integer, Activity> map = activityLoader.load(singleton(activityId));
        
        Activity activity = map.get(activityId);
        if(activity == null) {
            throw new FormNotFoundException(formClassId);
        }
        return new SiteCollection(activity, buildMapping(activity, formClassId), executor, 
                activityLoader.getPermissionCache());
    }
    
    
    private TableMapping buildMapping(Activity activity, ResourceId collectionId) {
        if(collectionId.getDomain() == CuidAdapter.ACTIVITY_DOMAIN) {
            return ActivityTableMappingBuilder.site(activity).build();
            
        } else {
            return ActivityTableMappingBuilder.reportingPeriod(activity).build();
        }
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

    @Override
    public Map<ResourceId, ResourceCollection> openCollections(QueryExecutor executor, Set<ResourceId> collectionIds) throws SQLException {
        Set<Integer> activityIds = new HashSet<>();
        for (ResourceId collectionId : collectionIds) {
            if(accept(collectionId)) {
                activityIds.add(CuidAdapter.getLegacyIdFromCuid(collectionId));
            }
        }
        if (activityIds.isEmpty()) {
            return Collections.emptyMap();
        
        } else {
            Map<Integer, Activity> activityMap = activityLoader.load(activityIds);
            Map<ResourceId, ResourceCollection> collectionMap = new HashMap<>();

            for (ResourceId collectionId : collectionIds) {
                if (accept(collectionId)) {
                    Activity activity = activityMap.get(CuidAdapter.getLegacyIdFromCuid(collectionId));
                    if (activity != null) {
                        collectionMap.put(collectionId,
                                new SiteCollection(activity, buildMapping(activity, collectionId), executor, 
                                        activityLoader.getPermissionCache()));
                    }
                }
            }
            return collectionMap;
        }
    }
}
