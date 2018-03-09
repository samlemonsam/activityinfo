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
package org.activityinfo.model.formula.functions;

import org.activityinfo.model.query.BooleanColumnView;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

public class IsNumberFunction extends UnaryFunctionBase {

    public static final IsNumberFunction INSTANCE = new IsNumberFunction();

    protected IsNumberFunction() {
        super("ISNUMBER");
    }

    @Override
    public FieldType resolveUnaryResultType(FieldType argumentType) {
        return BooleanType.INSTANCE;
    }

    @Override
    public FieldValue apply(FieldValue argument) {
        if(argument instanceof Quantity) {
            double doubleValue = ((Quantity) argument).getValue();
            return BooleanFieldValue.valueOf(!Double.isNaN(doubleValue));
        }
        return BooleanFieldValue.FALSE;
    }

    @Override
    public ColumnView columnApply(int numRows, ColumnView argument) {

        int[] result = new int[numRows];
        for (int i = 0; i < numRows; i++) {
            if(!Double.isNaN(argument.getDouble(i))) {
                result[i] = 1;
            }
        }

        return new BooleanColumnView(result);
    }
}
