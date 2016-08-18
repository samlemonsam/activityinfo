package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.service.store.FormPermissions;
import org.activityinfo.service.store.RecordVersion;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.cursor.RecordFetcher;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.update.BaseTableInserter;
import org.activityinfo.store.mysql.update.BaseTableUpdater;

import java.util.List;


public class SimpleTableAccessor implements FormAccessor {

    private final TableMapping mapping;
    private Authorizer authorizer;
    private QueryExecutor executor;

    public SimpleTableAccessor(TableMapping mapping, Authorizer authorizer, QueryExecutor executor) {
        this.mapping = mapping;
        this.authorizer = authorizer;
        this.executor = executor;
    }

    @Override
    public FormPermissions getPermissions(int userId) {
        return authorizer.getPermissions(userId);
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        return RecordFetcher.fetch(this, resourceId);
    }

    @Override
    public List<RecordVersion> getVersions(ResourceId resourceId) {
        throw new UnsupportedOperationException();
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
    public void update(RecordUpdate update) {
        BaseTableUpdater updater = new BaseTableUpdater(mapping, update.getResourceId());
        updater.update(executor, update);
    }

    @Override
    public void add(RecordUpdate update) {
        BaseTableInserter inserter = new BaseTableInserter(mapping, update.getResourceId());
        inserter.insert(executor, update);
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new SimpleTableColumnQueryBuilder(new MySqlCursorBuilder(mapping, executor));
    }

    @Override
    public long cacheVersion() {
        return mapping.getVersion();
    }
}
