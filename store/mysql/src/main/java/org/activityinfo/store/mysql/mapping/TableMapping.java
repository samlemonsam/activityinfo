package org.activityinfo.store.mysql.mapping;

import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;
import java.util.Map;


public class TableMapping {

    private String baseFromClause;
    private String baseFilter;
    private FormClass formClass;
    private PrimaryKeyMapping primaryKey;
    private Map<String, String> joins = Maps.newHashMap();

    private Map<ResourceId, FieldMapping> fieldMappings = Maps.newHashMap();

    TableMapping(String baseFromClause, String baseFilter, PrimaryKeyMapping primaryKey, List<FieldMapping> mappings, FormClass formClass) {
        this.baseFromClause = baseFromClause;
        this.primaryKey = primaryKey;
        this.baseFilter = baseFilter;
        this.formClass = formClass;
        for(FieldMapping mapping : mappings) {
            fieldMappings.put(mapping.getResourceId(), mapping);
        }
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
}
