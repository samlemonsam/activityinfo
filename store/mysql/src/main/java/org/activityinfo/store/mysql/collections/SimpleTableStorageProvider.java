package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.SimpleTable;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.store.spi.FormStorage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Provides storage for forms that are stored in a simple table, where each field maps more or less
 * to one table column.
 */
public class SimpleTableStorageProvider implements FormProvider {
    
    protected final SimpleTable table;
    protected final Authorizer authorizer;

    public SimpleTableStorageProvider(SimpleTable table, FormPermissions permissions) {
        this.table = table;
        this.authorizer = new ConstantAuthorizer(permissions);
    }

    public SimpleTableStorageProvider(SimpleTable table, Authorizer authorizer) {
        this.table = table;
        this.authorizer = authorizer;
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
