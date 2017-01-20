package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.FormPermissions;
import org.activityinfo.service.store.FormStorage;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.SimpleTable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class SimpleTableFormProvider implements FormProvider {
    
    private final SimpleTable table;
    private final Authorizer authorizer;

    public SimpleTableFormProvider(SimpleTable table, FormPermissions permissions) {
        this.table = table;
        this.authorizer = new ConstantAuthorizer(permissions);
    }

    @Override
    public boolean accept(ResourceId formId) {
        return table.accept(formId);
    }

    @Override
    public FormStorage openForm(QueryExecutor executor, ResourceId formId) throws SQLException {
        return new SimpleTableStorage(table.getMapping(executor, formId), authorizer, executor);
    }

    @Override
    public Optional<ResourceId> lookupForm(QueryExecutor executor, ResourceId recordId) throws SQLException {
        return table.lookupCollection(executor, recordId);
    }

    @Override
    public Map<ResourceId, FormStorage> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException {
        Map<ResourceId, FormStorage> map = new HashMap<>();
        for (ResourceId collectionId : formIds) {
            if(table.accept(collectionId)) {
                map.put(collectionId, openForm(executor, collectionId));
            }
        }
        return map;
    }

}
