package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.HrdStorageProvider;
import org.activityinfo.store.mysql.collections.FormProvider;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.spi.FormNotFoundException;
import org.activityinfo.store.spi.FormStorage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HrdProvider implements FormProvider {
    
    private HrdStorageProvider catalog = new HrdStorageProvider();
    
    @Override
    public boolean accept(ResourceId formId) {
        return formId.getDomain() == 'c';
    }

    @Override
    public FormStorage openForm(QueryExecutor executor, ResourceId formId) throws SQLException {
        Optional<FormStorage> collection = catalog.getForm(formId);
        if(!collection.isPresent()) {
            throw new FormNotFoundException(formId);
        }
        return collection.get();
    }

    @Override
    public Map<ResourceId, FormStorage> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException {
        Map<ResourceId, FormStorage> map = new HashMap<>();
        for (ResourceId resourceId : formIds) {
            if(accept(resourceId)) {
                map.put(resourceId, openForm(executor, resourceId));
            }
        }
        return map;
    }
}
