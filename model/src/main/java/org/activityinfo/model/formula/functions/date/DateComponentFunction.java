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
package org.activityinfo.model.formula.functions.date;

import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.formula.functions.ColumnFunction;
import org.activityinfo.model.formula.functions.FormulaFunction;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.time.LocalDate;

import java.util.List;

public abstract class DateComponentFunction extends FormulaFunction implements ColumnFunction {


    @Override
    public final FieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments, 1);

        FieldValue argument = arguments.get(0);
        if(!(argument instanceof LocalDate)) {
            throw new FormulaSyntaxException("Expected date argument");
        }
        LocalDate date = (LocalDate) argument;

        return new Quantity(apply(date));
    }

    @Override
    public final FieldType resolveResultType(List<FieldType> argumentTypes) {
        return new QuantityType(getUnits());
    }

    protected abstract String getUnits();

    protected abstract int apply(LocalDate date);

    protected int apply(String string) {
        return apply(LocalDate.parse(string));
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        checkArity(arguments, 1);

        ColumnView view = arguments.get(0);
        double result[] = new double[view.numRows()];

        for (int i = 0; i < view.numRows(); i++) {
            String date = view.getString(i);
            if(date == null) {
                result[i] = Double.NaN;
            } else {
                result[i] = apply(date);
            }
        }
        return new DoubleArrayColumnView(result);
    }
}

