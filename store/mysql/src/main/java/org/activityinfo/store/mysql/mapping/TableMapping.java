package org.activityinfo.store.mysql.mapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class TableMapping implements Serializable {

    private DeleteMethod deleteMethod;
    private String baseTable;
    private String baseFromClause;
    private String baseFilter;
    private FormClass formClass;
    private Map<String, Object> insertDefaults;
    private PrimaryKeyMapping primaryKey;
    private long version;

    private Map<ResourceId, FieldMapping> fieldMappings = Maps.newHashMap();

    TableMapping(String baseTable, String baseFromClause, String baseFilter, PrimaryKeyMapping primaryKey,
                 List<FieldMapping> mappings, FormClass formClass, DeleteMethod deleteMethod,
                 Map<String, Object> insertDefaults, long version) {
        this.baseTable = baseTable;
        this.baseFromClause = baseFromClause;
        this.primaryKey = primaryKey;
        this.baseFilter = baseFilter;
        this.formClass = formClass;
        this.insertDefaults = insertDefaults;
        this.deleteMethod = Preconditions.checkNotNull(deleteMethod, "deleteMethod");
        for(FieldMapping mapping : mappings) {
            fieldMappings.put(mapping.getFieldId(), mapping);
        }
        this.version = version;
    }

    public PrimaryKeyMapping getPrimaryKey() {
        return primaryKey;
    }

    public String getBaseFromClause() {
        return baseFromClause;
    }

    public FieldMapping getMapping(ResourceId fieldId) {
        return fieldMappings.get(fieldId);
    }

    public FormClass getFormClass() {
        return formClass;
    }

    public String getBaseFilter() {
        return baseFilter;
    }
    
    public void delete(QueryExecutor executor) {
        String sql = String.format("UPDATE %s SET dateDeleted = ?", getBaseTable());
        int rowsUpdated = executor.update(sql, Arrays.asList(new Date()));

        Preconditions.checkState(rowsUpdated == 1);
    }

    public Map<String, Object> getInsertDefaults() {
        return insertDefaults;
    }

    public boolean queryFields(QueryExecutor executor, Resource resource) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        Map<ResourceId, Integer> columnMap = new HashMap<>();
        int columnIndex = 1;
        for (Map.Entry<ResourceId, FieldMapping> entry : fieldMappings.entrySet()) {
            ResourceId fieldId = entry.getKey();
            columnMap.put(fieldId, columnIndex);
            for (String column : entry.getValue().getColumnNames()) {
                if(columnIndex > 1) {
                    sql.append(", ");
                }
                sql.append(column);
                columnIndex++;
            }
        }
        sql.append(" FROM ").append(baseFromClause)
           .append(" WHERE ")
                .append(primaryKey.getColumnName()).append("=")
                .append(CuidAdapter.getLegacyIdFromCuid(resource.getId()));

        try(ResultSet rs = executor.query(sql.toString())) {
            if(rs.next()) {

                for (Map.Entry<ResourceId, FieldMapping> entry : fieldMappings.entrySet()) {
                    FieldMapping fieldMapping = entry.getValue();
                    int firstColumnIndex = columnMap.get(entry.getKey());
                    FieldValue fieldValue = fieldMapping.getConverter().toFieldValue(rs, firstColumnIndex);
                    if (fieldValue != null) {
                        resource.set(entry.getKey(), fieldValue);
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public String getBaseTable() {
        return baseTable;
    }

    public DeleteMethod getDeleteMethod() {
        return deleteMethod;
    }

    public long getVersion() {
        return version;
    }
}
