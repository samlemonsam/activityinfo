package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.TypedRecordUpdate;

import java.util.Collections;
import java.util.List;

public class AdminFormStorage implements FormStorage {

    private FormStorage delegate;
    private final QueryExecutor executor;

    public AdminFormStorage(QueryExecutor executor, FormStorage delegate) {
        this.delegate = delegate;
        this.executor = executor;
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        return delegate.get(resourceId);
    }

    @Override
    public List<FormRecord> getSubRecords(ResourceId resourceId) {
        return delegate.getSubRecords(resourceId);
    }

    @Override
    public FormClass getFormClass() {
        return delegate.getFormClass();
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        delegate.getFormClass();
    }

    @Override
    public void add(TypedRecordUpdate update) {
        delegate.add(update);
        updateLevelVersion();
    }

    @Override
    public void update(TypedRecordUpdate update) {
        delegate.update(update);
        updateLevelVersion();
    }

    private void updateLevelVersion() {
        executor.update("update adminlevel set version = version + 1 where adminlevelid = ?",
                Collections.singletonList(
                        CuidAdapter.getLegacyIdFromCuid(delegate.getFormClass().getId())));
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return delegate.newColumnQuery();
    }

    @Override
    public long cacheVersion() {
        return delegate.cacheVersion();
    }

    @Override
    public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value) {
        delegate.updateGeometry(recordId, fieldId, value);
    }
}
