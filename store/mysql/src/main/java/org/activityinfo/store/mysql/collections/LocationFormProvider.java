package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.FormStorage;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.PermissionsCache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class LocationFormProvider implements FormProvider {

    private final PermissionsCache permissionsCache;

    public LocationFormProvider(PermissionsCache permissionsCache) {
        this.permissionsCache = permissionsCache;
    }


    @Override
    public boolean accept(ResourceId formId) {
        return formId.getDomain() == CuidAdapter.LOCATION_TYPE_DOMAIN;
    }

    @Override
    public FormStorage openForm(QueryExecutor executor, ResourceId formId) throws SQLException {
        return new LocationFormStorage(executor, formId, permissionsCache);
    }

    @Override
    public Optional<ResourceId> lookupForm(QueryExecutor queryExecutor, ResourceId recordId) throws SQLException {
        if (recordId.getDomain() == CuidAdapter.LOCATION_DOMAIN) {
            int locationId = CuidAdapter.getLegacyIdFromCuid(recordId);
            try (ResultSet rs = queryExecutor.query("SELECT locationTypeId FROM location WHERE locationId = " + locationId)) {
                if (rs.next()) {
                    int locationTypeId = rs.getInt(1);
                    return Optional.of(CuidAdapter.locationFormClass(locationTypeId));
                }
            }
        }
        return Optional.absent();
    }
    
    @Override
    public Map<ResourceId, FormStorage> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException {
        Map<ResourceId, FormStorage> result = new HashMap<>();
        for (ResourceId collectionId : formIds) {
            if(accept(collectionId)) {
                result.put(collectionId, openForm(executor, collectionId));
            }
        }
        return result;
    }

}
