package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.update.BaseTableUpdater;


public class SimpleTableCollection implements ResourceCollection {

    private final TableMapping mapping;
    private QueryExecutor executor;

    public SimpleTableCollection(TableMapping mapping, QueryExecutor executor) {
        this.mapping = mapping;
        this.executor = executor;
    }

    @Override
    public Optional<Resource> get(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass() {
        return mapping.getFormClass();
    }

    @Override
    public void update(ResourceUpdate update) {
        BaseTableUpdater updater = new BaseTableUpdater(mapping, update.getResourceId());
        updater.update(executor, update);
    }

    @Override
    public void add(ResourceUpdate update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new SimpleTableColumnQueryBuilder(new MySqlCursorBuilder(mapping, executor));
    }

}
