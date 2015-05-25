package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.cursor.QueryExecutor;


public class SimpleTableCollection implements ResourceCollection {

    private final TableMapping mapping;
    private QueryExecutor executor;

    public SimpleTableCollection(TableMapping mapping, QueryExecutor executor) {
        this.mapping = mapping;
        this.executor = executor;
    }

    @Override
    public Resource get(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass() {
        return mapping.getFormClass();
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new SimpleTableColumnQueryBuilder(new MySqlCursorBuilder(mapping, executor));
    }

}
