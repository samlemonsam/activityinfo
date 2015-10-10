package org.activityinfo.store.mysql.mapping;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.Join;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


/**
 * Maps a {@code FormField} to one or more MySQL columns
 */
public class FieldMapping implements Serializable {

    private final FormField formField;
    private List<String> columnNames = Lists.newArrayList();
    private FieldValueConverter converter;
    private Join join;

    public FieldMapping(FormField formField, String columnName, FieldValueConverter converter) {
        this.formField = formField;
        this.columnNames = Collections.singletonList(columnName);
        this.converter = converter;
    }

    public FieldMapping(FormField formField, List<String> columnName, FieldValueConverter converter) {
        this.formField = formField;
        this.columnNames = columnName;
        this.converter = converter;
    }

    public FieldMapping(FormField formField, String columnName, Join join, FieldValueConverter converter) {
        this.formField = formField;
        this.columnNames = Collections.singletonList(columnName);
        this.converter = converter;
        this.join = join;
    }


    public ResourceId getFieldId() {
        return formField.getId();
    }

    public FormField getFormField() {
        return formField;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public FieldValueConverter getConverter() {
        return converter;
    }

    public Join getJoin() {
        return join;
    }
}
