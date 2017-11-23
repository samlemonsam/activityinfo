package chdc.server;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.RecordVersion;
import org.activityinfo.store.spi.TypedRecordUpdate;

import java.util.List;

public class MySqlFormStorage implements FormStorage {

    private FormClass schema;

    public MySqlFormStorage(FormClass schema) {
        this.schema = schema;
    }

    @Override
    public FormPermissions getPermissions(int userId) {
        return FormPermissions.readWrite();
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<FormRecord> getSubRecords(ResourceId resourceId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<RecordVersion> getVersions(ResourceId recordId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<RecordVersion> getVersionsForParent(ResourceId parentRecordId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public FormClass getFormClass() {
        return schema;
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(TypedRecordUpdate update) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void update(TypedRecordUpdate update) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public long cacheVersion() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value) {
        throw new UnsupportedOperationException("TODO");
    }
}
