package chdc.server;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.json.Json;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.RecordVersion;
import org.activityinfo.store.spi.TypedRecordUpdate;
import org.jooq.Record;

import java.util.List;

import static chdc.server.sql.Tables.FORM_VERSION;
import static chdc.server.sql.Tables.RECORD;

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
        // Schemas are defined by the application and cannot be updated the user
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(TypedRecordUpdate update) {
        assert update.getFormId().equals(schema.getId());

        long newVersion = cacheVersion() + 1;
        String fields = Json.stringify(update.getChangedFieldValuesObject());

        // Insert the new record with all of its values
        ChdcDatabase.sql()
                .insertInto(RECORD, RECORD.FORM_ID, RECORD.RECORD_ID, RECORD.VERSION, RECORD.FIELDS)
                .values(schema.getId().asString(), update.getRecordId().asString(), newVersion, fields)
                .execute();

        // Update the form version
        ChdcDatabase.sql()
                .insertInto(FORM_VERSION, FORM_VERSION.FORM_ID, FORM_VERSION.VERSION)
                .values(schema.getId().asString(), newVersion)
                .onDuplicateKeyUpdate()
                .set(FORM_VERSION.VERSION, newVersion)
                .execute();

    }

    @Override
    public void update(TypedRecordUpdate update) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new MySqlColumnQueryBuilder(schema);
    }

    @Override
    public long cacheVersion() {
        Record record = ChdcDatabase.sql().select()
                .from(FORM_VERSION)
                .where(FORM_VERSION.FORM_ID.eq(schema.getId().asString()))
                .fetchOne();

        if(record == null) {
            return 1L;
        } else {
            return record.getValue(FORM_VERSION.VERSION);
        }
    }

    @Override
    public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value) {
        throw new UnsupportedOperationException("TODO");
    }
}
