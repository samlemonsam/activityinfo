package org.activityinfo.store.mysql;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionAccessor;
import org.activityinfo.service.store.CursorBuilder;
import org.activityinfo.service.store.Cursor;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.cursor.QueryExecutor;


public class MySqlCollectionAccessor implements CollectionAccessor {

    private final TableMapping mapping;
    private QueryExecutor executor;

    public MySqlCollectionAccessor(TableMapping mapping, QueryExecutor executor) {
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
    public CursorBuilder newCursor() {
        return new MySqlCursorBuilder(mapping, executor);
    }

    @Override
    public Cursor openCursor(ResourceId formClassId) throws Exception {
        throw new UnsupportedOperationException();
    }
}
