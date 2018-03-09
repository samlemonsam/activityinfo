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

import org.activityinfo.model.formula.diagnostic.ArgumentException;
import org.activityinfo.model.formula.diagnostic.InvalidTypeException;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

public abstract class RoundingOperator extends UnaryFunctionBase {

    protected RoundingOperator(String name) { super(name); }

    @Override
    public FieldValue apply(FieldValue argument) {
        if(!(argument instanceof Quantity)) {
            throw new InvalidTypeException("Expected QUANTITY value");
        }
        Quantity quantity = (Quantity) argument;
        return new Quantity(apply(quantity.getValue()));
    }

    @Override
    public FieldType resolveUnaryResultType(FieldType argumentType) {
        if (!(argumentType instanceof QuantityType)) {
            throw new ArgumentException(0, "Expected QUANTITY value");
        }
        return new QuantityType();
    }

    @Override
    public ColumnView columnApply(int numRows, ColumnView argument) {
        double[] result = new double[numRows];
        for(int i=0;i<numRows;i++) {
            result[i] = apply(argument.getDouble(i));
        }
        return new DoubleArrayColumnView(result);
    }

    public abstract double apply(double argument);

}
