package org.activityinfo.store.mysql.mapping;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.Join;

import java.util.Collections;
import java.util.List;


public class FieldMapping {

    private final FormField formField;
    private List<String> columnNames = Lists.newArrayList();
    private FieldValueMapping valueExtractor;
    private Join join;

    public FieldMapping(FormField formField, String columnName, FieldValueMapping valueExtractor) {
        this.formField = formField;
        this.columnNames = Collections.singletonList(columnName);
        this.valueExtractor = valueExtractor;
    }

    public FieldMapping(FormField formField, List<String> columnName, FieldValueMapping valueExtractor) {
        this.formField = formField;
        this.columnNames = columnName;
        this.valueExtractor = valueExtractor;
    }

    public FieldMapping(FormField formField, String columnName, Join join, FieldValueMapping valueExtractor) {
        this.formField = formField;
        this.columnNames = Collections.singletonList(columnName);
        this.valueExtractor = valueExtractor;
        this.join = join;
    }


    public ResourceId getResourceId() {
        return formField.getId();
    }

    public FormField getFormField() {
        return formField;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public FieldValueMapping getValueExtractor() {
        return valueExtractor;
    }

    public Join getJoin() {
        return join;
    }
}
