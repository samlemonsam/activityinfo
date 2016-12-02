package org.activityinfo.store.mysql.mapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


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
