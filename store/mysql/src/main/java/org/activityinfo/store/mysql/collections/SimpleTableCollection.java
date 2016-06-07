package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.update.BaseTableInserter;
import org.activityinfo.store.mysql.update.BaseTableUpdater;

import java.sql.SQLException;


public class SimpleTableCollection implements ResourceCollection {

    private final TableMapping mapping;
    private Authorizer authorizer;
    private QueryExecutor executor;

    public SimpleTableCollection(TableMapping mapping, Authorizer authorizer, QueryExecutor executor) {
        this.mapping = mapping;
        this.authorizer = authorizer;
        this.executor = executor;
    }

    @Override
    public CollectionPermissions getPermissions(int userId) {
        return authorizer.getPermissions(userId);
    }

    @Override
    public Optional<Resource> get(ResourceId resourceId) {
        Resource resource = Resources.createResource();
        resource.setId(resourceId);
        resource.setOwnerId(getFormClass().getId());

        try {
            if(mapping.queryFields(executor, resource)) {
                return Optional.of(resource);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.absent();
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
    public void update(ResourceUpdate update) {
        BaseTableUpdater updater = new BaseTableUpdater(mapping, update.getResourceId());
        updater.update(executor, update);
    }

    @Override
    public void add(ResourceUpdate update) {
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
