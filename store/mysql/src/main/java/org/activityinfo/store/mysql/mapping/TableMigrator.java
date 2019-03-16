package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.op.CreateOrUpdateForm;
import org.activityinfo.store.hrd.op.CreateOrUpdateRecord;
import org.activityinfo.store.mysql.collections.AdminEntityTable;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.spi.TypedRecordUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableMigrator {

    public static TableMapping findMapping(ResourceId formId, QueryExecutor executor) throws SQLException {
        AdminEntityTable adminEntityTable = new AdminEntityTable();
        if(adminEntityTable.accept(formId)) {
            return adminEntityTable.getMapping(executor, formId);
        }
        throw new IllegalArgumentException("No acceptable table mapping for " + formId);
    }

    public static void migrate(ResourceId formId, QueryExecutor executor) throws SQLException {
        migrate(formId, findMapping(formId, executor), executor);
    }

    private static class Binding {
        private FieldMapping mapping;
        private int startIndex;

        public Binding(FieldMapping mapping, int startIndex) {
            this.mapping = mapping;
            this.startIndex = startIndex;
        }
    }

    public static void migrate(ResourceId formId, TableMapping mapping, QueryExecutor executor) throws SQLException {

        Hrd.ofy().transact(new CreateOrUpdateForm(mapping.getFormClass()));

        List<Binding> bindings = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(mapping.getPrimaryKey().getColumnName());

        int nextIndex = 2;
        for (FormField field : mapping.getFormClass().getFields()) {
            FieldMapping fieldMapping = mapping.getMapping(field.getId());
            bindings.add(new Binding(fieldMapping, nextIndex));

            for (String columnName : fieldMapping.getColumnNames()) {
                sql.append(", ").append(columnName);
            }

            nextIndex += fieldMapping.getColumnNames().size();
        }

        sql.append(" FROM ")
                .append(mapping.getBaseFromClause())
                .append(" WHERE ")
                .append(mapping.getBaseFilter());

        System.out.println(sql);

        char domain = mapping.getPrimaryKey().getDomain();

        List<TypedRecordUpdate> batch = new ArrayList<>();

        ResultSet rs = executor.query(sql.toString());
        while (rs.next()) {
            int id = rs.getInt(1);
            ResourceId recordId = CuidAdapter.cuid(domain, id);
            TypedFormRecord record = new TypedFormRecord(recordId, formId);

            for (Binding binding : bindings) {
                record.set(
                        binding.mapping.getFieldId(),
                        binding.mapping.getConverter().toFieldValue(rs, binding.startIndex));
            }

            batch.add(new TypedRecordUpdate(3, record));

            if(batch.size() > 25) {
                Hrd.ofy().transact(new CreateOrUpdateRecord(formId, batch));
                batch.clear();
            }
        }

        if(!batch.isEmpty()) {
            Hrd.ofy().transact(new CreateOrUpdateRecord(formId, batch));
        }
    }
}
