package chdc.server;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.CursorObserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlColumnQueryBuilder implements ColumnQueryBuilder {

    private class FieldExtractor {
        private int columnIndex;
        private final FormField field;
        private final CursorObserver<FieldValue> observer;

        private FieldExtractor(int columnIndex, FormField field, CursorObserver<FieldValue> observer) {
            this.columnIndex = columnIndex;
            this.field = field;
            this.observer = observer;
        }

        public void onNext(ResultSet resultSet) throws SQLException {
            String jsonString = resultSet.getString(columnIndex);
            if(jsonString == null) {
                observer.onNext(null);

            } else {
                JsonValue jsonValue = Json.parse(jsonString);
                FieldValue fieldValue = field.getType().parseJsonValue(jsonValue);

                observer.onNext(fieldValue);
            }
        }

        public String getSqlExpression() {
            // Field values are stored in a MySQL JSON field
            return "fields->\"$." + field.getId().asString() + "\"";
        }
    }

    private FormClass schema;
    private int nextColumnIndex = 2;

    private List<CursorObserver<ResourceId>> idObservers = new ArrayList<>(0);
    private List<FieldExtractor> fieldExtractors = new ArrayList<>();

    public MySqlColumnQueryBuilder(FormClass schema) {
        this.schema = schema;
    }

    @Override
    public void only(ResourceId resourceId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        idObservers.add(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        FormField field = schema.getField(fieldId);

        fieldExtractors.add(new FieldExtractor(nextColumnIndex++, field, observer));
    }

    @Override
    public void execute() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT record_id ");

        for (FieldExtractor field : fieldExtractors) {
            sql.append(", ").append(field.getSqlExpression());
        }

        sql.append(" FROM record ");
        sql.append(" WHERE form_id = ?" );
        sql.append(" ORDER BY record_id");

        try(PreparedStatement statement = ChdcDatabase.getConnection().prepareStatement(sql.toString())) {
            statement.setString(1, schema.getId().asString());

            try(ResultSet resultSet = statement.executeQuery()) {

                while(resultSet.next()) {
                    ResourceId recordId = ResourceId.valueOf(resultSet.getString(1));
                    for (CursorObserver<ResourceId> idObserver : idObservers) {
                        idObserver.onNext(recordId);
                    }

                    for (FieldExtractor fieldExtractor : fieldExtractors) {
                        fieldExtractor.onNext(resultSet);
                    }
                }
            }

            for (CursorObserver<ResourceId> idObserver : idObservers) {
                idObserver.done();
            }
            for (FieldExtractor fieldExtractor : fieldExtractors) {
                fieldExtractor.observer.done();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
