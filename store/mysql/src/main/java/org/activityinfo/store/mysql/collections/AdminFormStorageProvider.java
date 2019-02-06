package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.spi.FormStorage;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminFormStorageProvider implements FormProvider {
    private SimpleTableStorageProvider delegate;

    public AdminFormStorageProvider() {
        this.delegate = new SimpleTableStorageProvider(new AdminEntityTable());
    }

    @Override
    public boolean accept(ResourceId formId) {
        return delegate.accept(formId);
    }

    @Override
    public FormStorage openForm(QueryExecutor executor, ResourceId formId) throws SQLException {
        return new AdminFormStorage(executor, delegate.openForm(executor, formId));
    }

    @Override
    public Map<ResourceId, FormStorage> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException {
        return delegate.openForms(executor, formIds)
                .values()
                .stream()
                .map(s -> new AdminFormStorage(executor, s))
                .collect(Collectors.toMap(s -> s.getFormClass().getId(), s -> s));
    }
}
