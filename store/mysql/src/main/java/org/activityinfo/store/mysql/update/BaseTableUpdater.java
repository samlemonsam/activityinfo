package org.activityinfo.store.mysql.update;

import com.google.common.base.Preconditions;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.FieldMapping;
import org.activityinfo.store.mysql.mapping.TableMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Builds an update to an existing base table row.
 * 
 */
public class BaseTableUpdater {
    private TableMapping mapping;
    
    private List<String> updates = new ArrayList<>();
    private List<Object> updateParameters = new ArrayList<>();
    
    private List<Object> parameters = new ArrayList<>();

    private final int siteId;
    
    private boolean deleted = false;

    public BaseTableUpdater(TableMapping mapping, ResourceId siteId) {
        this.mapping = mapping;
        this.siteId = CuidAdapter.getLegacyIdFromCuid(siteId);
    }

    public void update(ResourceId fieldId, FieldValue value) {
        FieldMapping fieldMapping = mapping.getMapping(fieldId);
        Preconditions.checkArgument(fieldMapping != null, "No such field: %s", fieldId.asString());

        if(value == null) {
            clearValue(fieldMapping);
        } else {
            updateValue(fieldMapping, value);
        }
    }
    
    public void delete() {
        deleted = true;
    }

    private void updateValue(FieldMapping fieldMapping, FieldValue value) {
        for (String column : fieldMapping.getColumnNames()) {
            updates.add(format("%s = ?", column));
        }
        updateParameters.addAll(fieldMapping.getValueExtractor().toParameters(value));
    }

    private void clearValue(FieldMapping fieldMapping) {
        Preconditions.checkArgument(!fieldMapping.getFormField().isRequired(),
                "Field %s ('%s') is required and cannot be set to null");

        for (String column : fieldMapping.getColumnNames()) {
            updates.add(format("%s = NULL", column));
        }
    }


    public void update(QueryExecutor executor, ResourceUpdate update) {
        Preconditions.checkArgument(update.getResourceId().getDomain() == mapping.getPrimaryKey().getDomain(),
                "Resource Id mismatch, expected domain '%c', got id '%s'",
                mapping.getPrimaryKey().getDomain(),
                update.getResourceId().asString());

        // Describe all the updates
        for (Map.Entry<ResourceId, FieldValue> change : update.getChangedFieldValues().entrySet()) {
            update(change.getKey(), change.getValue());
        }

        executeUpdates(executor);   
    }

    public void executeUpdates(QueryExecutor executor) {
        StringBuilder sql = new StringBuilder("UPDATE ").append(mapping.getBaseTable()).append(" ");
        appendSetClauses(sql);
        appendWhereClause(sql);

        System.out.println(sql.toString());
        System.out.println(parameters.toString());

        int rowsUpdated = executor.update(sql.toString(), parameters);

        Preconditions.checkState(rowsUpdated == 1);
    }

    private void appendSetClauses(StringBuilder sql) {
        sql.append(" SET ");
        if(deleted) {
            sql.append(" dateDeleted = ?");
            parameters.add(new Date());
        } else {
            sql.append(" dateDeleted = NULL");
        }
        for (String update : updates) {
            sql.append(", ").append(update);
        }
        parameters.addAll(updateParameters);
    }

    private void appendWhereClause(StringBuilder sql) {
        sql.append(" WHERE ").append(mapping.getPrimaryKey().getColumnName()).append(" = ?");
        parameters.add(siteId);
    }

}
