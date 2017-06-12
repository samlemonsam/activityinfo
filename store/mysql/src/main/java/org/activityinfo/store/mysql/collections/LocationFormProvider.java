package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.PermissionsCache;
import org.activityinfo.store.spi.FormStorage;

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
        return formId.getDomain() == CuidAdapter.LOCATION_TYPE_DOMAIN &&
                CuidAdapter.isValidLegacyId(formId);
    }

    @Override
    public FormStorage openForm(QueryExecutor executor, ResourceId formId) throws SQLException {
        return new LocationFormStorage(executor, formId, permissionsCache);
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
