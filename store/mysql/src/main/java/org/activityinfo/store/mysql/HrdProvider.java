package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.service.store.FormNotFoundException;
import org.activityinfo.store.hrd.HrdCatalog;
import org.activityinfo.store.mysql.collections.FormProvider;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HrdProvider implements FormProvider {
    
    private HrdCatalog catalog = new HrdCatalog();
    
    @Override
    public boolean accept(ResourceId formId) {
        return formId.getDomain() == 'c';
    }

    @Override
    public FormAccessor openForm(QueryExecutor executor, ResourceId formId) throws SQLException {
        Optional<FormAccessor> collection = catalog.getForm(formId);
        if(!collection.isPresent()) {
            throw new FormNotFoundException(formId);
        }
        return collection.get();
    }

    @Override
    public Optional<ResourceId> lookupForm(QueryExecutor executor, ResourceId recordId) throws SQLException {
        Optional<FormAccessor> collection = catalog.lookupForm(recordId);
        if(collection.isPresent()) {
            return Optional.of(collection.get().getFormClass().getId());
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Map<ResourceId, FormAccessor> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException {
        Map<ResourceId, FormAccessor> map = new HashMap<>();
        for (ResourceId resourceId : formIds) {
            if(accept(resourceId)) {
                map.put(resourceId, openForm(executor, resourceId));
            }
        }
        return map;
    }
}
