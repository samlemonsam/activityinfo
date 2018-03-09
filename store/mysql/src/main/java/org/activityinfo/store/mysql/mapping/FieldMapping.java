/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
