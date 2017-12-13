package org.activityinfo.model.type.expr;
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

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;

/**
 * @author yuriyz on 8/14/14.
 */
public class CalculatedValue implements FieldValue {

    private FieldValue value;

    public CalculatedValue(FieldValue value) {
        this.value = value;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return CalculatedFieldType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJson() {
        return Json.createNull();
    }

    public String asString() {
        if (value instanceof Quantity) {
            return Double.toString(((Quantity) value).getValue());
        } else if (value instanceof BooleanFieldValue) {
            return Boolean.toString(((BooleanFieldValue) value).asBoolean());
        }
        return "";
    }
}
