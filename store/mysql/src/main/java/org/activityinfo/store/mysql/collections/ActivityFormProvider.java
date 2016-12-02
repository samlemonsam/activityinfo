package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.service.store.FormNotFoundException;
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


public class ActivityFormProvider implements FormProvider {
    
    private static final Logger LOGGER = Logger.getLogger(ActivityFormProvider.class.getName());

    /**
     * Caches activities within a session to avoid fetching Activity for both Site and ReportingFrequency table
     */
    private ActivityLoader activityLoader;

    public ActivityFormProvider(ActivityLoader activityLoader) {
        this.activityLoader = activityLoader;
    }

    @Override
    public boolean accept(ResourceId formId) {
        return formId.getDomain() == CuidAdapter.ACTIVITY_DOMAIN ||
               formId.getDomain() == CuidAdapter.MONTHLY_REPORT_FORM_CLASS;
    }

    @Override
    public FormAccessor openForm(QueryExecutor executor, ResourceId formId) throws SQLException {
        int activityId = CuidAdapter.getLegacyIdFromCuid(formId);
        Map<Integer, Activity> map = activityLoader.load(singleton(activityId));
        
        Activity activity = map.get(activityId);
        if(activity == null) {
            throw new FormNotFoundException(formId);
        }
        return new SiteFormAccessor(activity, buildMapping(activity, formId), executor, 
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
    public Optional<ResourceId> lookupForm(QueryExecutor executor, ResourceId recordId) throws SQLException {
        if(recordId.getDomain() == CuidAdapter.SITE_DOMAIN) {
            int siteId = CuidAdapter.getLegacyIdFromCuid(recordId);
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
    public Map<ResourceId, FormAccessor> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException {
        Set<Integer> activityIds = new HashSet<>();
        for (ResourceId collectionId : formIds) {
            if(accept(collectionId)) {
                activityIds.add(CuidAdapter.getLegacyIdFromCuid(collectionId));
            }
        }
        if (activityIds.isEmpty()) {
            return Collections.emptyMap();
        
        } else {
            Map<Integer, Activity> activityMap = activityLoader.load(activityIds);
            Map<ResourceId, FormAccessor> collectionMap = new HashMap<>();

            for (ResourceId collectionId : formIds) {
                if (accept(collectionId)) {
                    Activity activity = activityMap.get(CuidAdapter.getLegacyIdFromCuid(collectionId));
                    if (activity != null) {
                        collectionMap.put(collectionId,
                                new SiteFormAccessor(activity, buildMapping(activity, collectionId), executor, 
                                        activityLoader.getPermissionCache()));
                    }
                }
            }
            return collectionMap;
        }
    }
}
