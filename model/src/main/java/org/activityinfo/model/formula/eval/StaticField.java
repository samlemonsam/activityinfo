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
package org.activityinfo.model.formula.eval;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NullFieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

public class StaticField implements FieldValueSource {

    private FormField field;

    public StaticField(FormField field) {
        this.field = field;
    }

    @Override
    public FieldValue getValue(TypedFormRecord instance, EvalContext context) {
        FieldValue fieldValue = instance.get(field.getId(), field.getType().getTypeClass());
        if (fieldValue != null) {
            return fieldValue;
        } else {
            // we don't want to get NPE in ComparisonOperator

            if (field.getType() instanceof QuantityType) {
                return new Quantity(0);
            }

            return NullFieldValue.INSTANCE;
        }
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        return field.getType();
    }

    public FormField getField() {
        return field;
    }
}
