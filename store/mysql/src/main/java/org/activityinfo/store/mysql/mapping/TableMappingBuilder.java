package org.activityinfo.store.mysql.mapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.primitive.TextValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public class TableMappingBuilder {
    private String tableName;
    private List<FieldMapping> mappings = Lists.newArrayList();
    private PrimaryKeyMapping primaryKeyMapping;
    private FormClass formClass;

    private TableMappingBuilder(String tableName) {
        this.tableName = tableName;
    }

    public static TableMappingBuilder newMapping(String tableName) {
        return new TableMappingBuilder(tableName);
    }

    public void setFormClass(FormClass formClass) {
        this.formClass = formClass;
    }

    public void setPrimaryKeyMapping(char domain, String columnName) {
        this.primaryKeyMapping = new PrimaryKeyMapping(domain, columnName);
    }

    public void addTextField(FormField field, String columnName) {
        mappings.add(new FieldMapping(field, columnName, TEXT_EXTRACTOR));
    }

    public void addReferenceField(FormField field, final char domain, String columnName) {
        mappings.add(new FieldMapping(field, columnName, new FieldValueExtractor() {
            @Override
            public FieldValue extract(ResultSet rs, int index) throws SQLException {
                int id = rs.getInt(index);
                if(rs.wasNull()) {
                    return null;
                } else {
                    return new ReferenceValue(CuidAdapter.cuid(domain, id));
                }
            }
        }));
    }

    private static final FieldValueExtractor TEXT_EXTRACTOR = new FieldValueExtractor() {

        @Override
        public FieldValue extract(ResultSet rs, int index) throws SQLException {
            return TextValue.valueOf(rs.getString(index));
        }
    };

    public TableMapping build() {
        Preconditions.checkState(formClass != null, tableName + ": FormClass is not set");
        Preconditions.checkState(primaryKeyMapping != null, tableName + ": Primary key is not set");
        return new TableMapping(tableName, primaryKeyMapping, mappings, formClass);
    }
}
