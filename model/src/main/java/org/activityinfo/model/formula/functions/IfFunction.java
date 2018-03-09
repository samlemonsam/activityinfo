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
import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.query.*;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.Arrays;
import java.util.List;


public class IfFunction extends FormulaFunction implements ColumnFunction {


    public static final IfFunction INSTANCE = new IfFunction();

    private IfFunction() {
    }

    @Override
    public String getId() {
        return "if";
    }

    @Override
    public String getLabel() {
        return "IF";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments, 3);

        BooleanFieldValue condition = toBoolean(arguments.get(0));
        FieldValue ifTrue = arguments.get(1);
        FieldValue ifFalse = arguments.get(2);

        if(condition == BooleanFieldValue.TRUE) {
            return ifTrue;
        } else {
            return ifFalse;
        }
    }

    private BooleanFieldValue toBoolean(FieldValue fieldValue) {
        if(fieldValue instanceof BooleanFieldValue) {
            return (BooleanFieldValue) fieldValue;
        } else {
            throw new FormulaSyntaxException("IF() condition must boolean.");
        }
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        checkArity(argumentTypes, 3);

        FieldType conditionType = argumentTypes.get(0);
        if(!(conditionType instanceof BooleanType)) {
            throw new ArgumentException(0, "Expected TRUE/FALSE value");
        }

        FieldType trueType = argumentTypes.get(1);
        FieldType falseType = argumentTypes.get(2);
        if(trueType.getTypeClass() != falseType.getTypeClass()) {
            throw new ArgumentException(2, "Must have the same type as the TRUE argument.");
        }
        return trueType;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        checkArity(arguments, 3);

        ColumnView condition = arguments.get(0);
        ColumnView x = arguments.get(1);
        ColumnView y = arguments.get(2);

        if(x.getType() == ColumnType.NUMBER) {
            double values[] = new double[x.numRows()];
            Arrays.fill(values, Double.NaN);

            for (int i = 0; i < values.length; i++) {
                switch (condition.getBoolean(i)) {
                    case ColumnView.TRUE:
                        values[i] = x.getDouble(i);
                        break;
                    case ColumnView.FALSE:
                        values[i] = y.getDouble(i);
                        break;
                }
            }
            return new DoubleArrayColumnView(values);

        } else if(x.getType() == ColumnType.BOOLEAN) {
            int values[] = new int[x.numRows()];
            Arrays.fill(values, ColumnView.NA);
            for (int i = 0; i < values.length; i++) {
                switch (condition.getBoolean(i)) {
                    case ColumnView.TRUE:
                        values[i] = x.getBoolean(i);
                        break;
                    case ColumnView.FALSE:
                        values[i] = y.getBoolean(i);
                        break;
                }
            }
            return new BooleanColumnView(values);

        } else if(x.getType() == ColumnType.STRING) {
            String[] values = new String[x.numRows()];
            for (int i = 0; i < values.length; i++) {
                switch (condition.getBoolean(i)) {
                    case ColumnView.TRUE:
                        values[i] = x.getString(i);
                        break;
                    case ColumnView.FALSE:
                        values[i] = y.getString(i);
                        break;
                }
            }
            return new StringArrayColumnView(values);
        } else {
            throw new IllegalStateException("type: " + x.getType());
        }
    }
}