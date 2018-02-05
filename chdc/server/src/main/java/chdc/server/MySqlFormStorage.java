package chdc.server;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.TypedRecordUpdate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        try(PreparedStatement stmt = ChdcDatabase.getConnection().prepareStatement(
                "SELECT fields FROM record WHERE form_id = ? AND record_id = ?")) {

            stmt.setString(1, schema.getId().asString());
            stmt.setString(2, resourceId.asString());

            try(ResultSet rs = stmt.executeQuery()) {
                if(!rs.next()) {
                    return Optional.absent();
                }
                JsonValue fieldMap = Json.parse(rs.getString(1));
                FormRecord record = new FormRecord(new RecordRef(schema.getId(), resourceId), null, fieldMap);

                return Optional.of(record);
            }

        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FormRecord> getSubRecords(ResourceId resourceId) {
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

        try(PreparedStatement stmt = ChdcDatabase.getConnection().prepareStatement(
                "INSERT INTO record (form_id, record_id, version, fields) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, schema.getId().asString());
            stmt.setString(2, update.getRecordId().asString());
            stmt.setLong(3, newVersion);
            stmt.setString(4, fields);

        } catch(SQLException e) {
            throw new RuntimeException(e);
        }

        try(PreparedStatement stmt = ChdcDatabase.getConnection().prepareStatement(
                "INSERT INTO form_version (form_id, version) VALUES (?, ?)")) {

            stmt.setString(1, schema.getId().asString());
            stmt.setLong(2, newVersion);

        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
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

        try(PreparedStatement stmt = ChdcDatabase.getConnection().prepareStatement(
                "SELECT version FROM form_version WHERE form_id = ?")) {

            stmt.setString(1, schema.getId().asString());
            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    return rs.getLong(1);
                } else {
                    return 1L;
                }
            }

        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value) {
        throw new UnsupportedOperationException("TODO");
    }
}
