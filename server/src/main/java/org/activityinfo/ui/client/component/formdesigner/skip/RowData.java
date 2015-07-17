package org.activityinfo.ui.client.component.formdesigner.skip;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.activityinfo.model.expr.functions.ExprFunction;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;

import java.util.Set;

/**
 * @author yuriyz on 7/25/14.
 */
public class RowData {

    private FormField formField;
    private ExprFunction joinFunction = RowDataBuilder.DEFAULT_JOIN_FUNCTION;
    private ExprFunction function;
    private FieldValue value;

    public RowData() {
    }

    public RowData(FormField formField, ExprFunction joinFunction, ExprFunction function,
                   FieldValue value) {
        this.formField = formField;
        this.joinFunction = joinFunction;
        this.function = function;
        this.value = value;
    }

    public FormField getFormField() {
        return formField;
    }

    public void setFormField(FormField formField) {
        this.formField = formField;
    }

    public ExprFunction getJoinFunction() {
        return joinFunction;
    }

    public void setJoinFunction(ExprFunction joinFunction) {
        this.joinFunction = joinFunction;
    }

    public ExprFunction getFunction() {
        return function;
    }

    public void setFunction(ExprFunction function) {
        this.function = function;
    }

    public FieldValue getValue() {
        return value;
    }

    public void setValue(FieldValue value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RowData rowData = (RowData) o;

        if (formField != null ? !formField.equals(rowData.formField) : rowData.formField != null) return false;
        if (function != null ? !function.equals(rowData.function) : rowData.function != null) return false;
//        if (joinFunction != null ? !joinFunction.equals(rowData.joinFunction) : rowData.joinFunction != null)
//            return false;
        if (value != null ? !value.equals(rowData.value) : rowData.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = formField != null ? formField.hashCode() : 0;
//        result = 31 * result + (joinFunction != null ? joinFunction.hashCode() : 0);
        result = 31 * result + (function != null ? function.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(formField == null ? "NULL" : formField.getId());
        sb.append(" ");
        sb.append(function == null ? "NULL" : function.getId());
        sb.append(" ");
        sb.append(value);
        return sb.toString();
    }
}
