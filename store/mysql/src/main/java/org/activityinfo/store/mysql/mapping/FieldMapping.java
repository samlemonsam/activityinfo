package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.Join;


public class FieldMapping {

    private final FormField formField;
    private String columnName;
    FieldValueExtractor valueExtractor;

    public FieldMapping(FormField field, String columnName, FieldValueExtractor valueExtractor, Join join) {
        this.formField = field;
        this.columnName = columnName;
        this.valueExtractor = valueExtractor;
    }

    public FieldMapping(FormField formField, String columnName, FieldValueExtractor valueExtractor) {
        this.formField = formField;
        this.columnName = columnName;
        this.valueExtractor = valueExtractor;
    }

    public ResourceId getResourceId() {
        return formField.getId();
    }

    public FormField getFormField() {
        return formField;
    }

    public String getColumnName() {
        return columnName;
    }

    public FieldValueExtractor getValueExtractor() {
        return valueExtractor;
    }

}
