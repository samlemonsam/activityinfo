package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.cursor.ResourceFetcher;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.metadata.DatabaseTargetForm;


public class TargetCollection implements ResourceCollection {
    
    private final QueryExecutor executor;
    private final DatabaseTargetForm target;
    private final TableMapping mapping;
    
    public TargetCollection(QueryExecutor executor, DatabaseTargetForm target) {
        this.target = target;
        this.executor = executor;
        this.mapping = target.buildMapping();
    }

    @Override
    public CollectionPermissions getPermissions(int userId) {
        return CollectionPermissions.readonly();
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        return ResourceFetcher.fetch(this, resourceId);
    }

    @Override
    public FormClass getFormClass() {
        return mapping.getFormClass();
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(RecordUpdate update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(RecordUpdate update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new TargetQueryBuilder(executor, target, mapping);
    }

    @Override
    public long cacheVersion() {
        return 0;
    }
}
