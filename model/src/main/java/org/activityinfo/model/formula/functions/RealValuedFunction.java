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

import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.formula.diagnostic.InvalidTypeException;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

import java.util.List;

public abstract class RealValuedFunction extends FormulaFunction implements ColumnFunction {

    private String name;

    protected RealValuedFunction(String name) {
        this.name = name;
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        //must be unary or binary function
        if(arguments.size() == 1) {
            return unaryFunctionCall(arguments.get(0));
        } else if(arguments.size() == 2) {
            return binaryFunctionCall(arguments);
        } else {
            throw new FormulaSyntaxException("The " + getLabel() + "() function expects exactly 1 or 2 argument(s).");
        }
    }

    private FieldValue unaryFunctionCall(FieldValue argument) {
        Quantity q = Casting.toQuantity(argument);

        if(Double.isNaN(q.getValue())) {
            return new Quantity(Double.NaN);
        }

        double d = toDouble(q);
        double value = apply(d);

        if(Double.isNaN(value)) {
            return new Quantity(Double.NaN);
        } else {
            return new Quantity(value);
        }
    }

    private FieldValue binaryFunctionCall(List<FieldValue> arguments) {
        // Excel-style (sort-of) missing value handling
        // (1) If at least one value is non-missing, however, treat the other as zero
        // (2) If both values are missing, then the result is also missing (NaN)
        // The first case is consistent with Excel, and the second with SQL and R

        Quantity qa = Casting.toQuantity(arguments.get(0));
        Quantity qb = Casting.toQuantity(arguments.get(1));

        if(Double.isNaN(qa.getValue()) && Double.isNaN(qb.getValue())) {
            return new Quantity(Double.NaN);
        }
        double da = toDouble(qa);
        double db = toDouble(qb);

        double value = apply(da, db);
        if(Double.isNaN(value)) {
            return new Quantity(Double.NaN);
        } else {
            return new Quantity(value);
        }
    }

    private double toDouble(Quantity quantity) {
        double d = quantity.getValue();
        if(Double.isNaN(d)) {
            return 0d;
        }
        return d;
    }

    @Override
    public final boolean isInfix() {
        return true;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        //must be unary or binary function
        if(arguments.size() == 1) {
            return unaryFunctionColumnCall(numRows,arguments.get(0));
        } else if(arguments.size() == 2) {
            return binaryFunctionColumnCall(numRows,arguments);
        } else {
            throw new FormulaSyntaxException("The " + getLabel() + "() function expects exactly 1 or 2 argument(s).");
        }
    }

    private ColumnView unaryFunctionColumnCall(int numRows, ColumnView argument) {
        double[] result = new double[numRows];
        for (int i = 0; i < numRows; i++) {
            double d = argument.getDouble(i);
            if(Double.isNaN(d)) {
                result[i] = Double.NaN;
            } else {
                result[i] = apply(d);
            }
        }
        return new DoubleArrayColumnView(result);
    }

    private ColumnView binaryFunctionColumnCall(int numRows, List<ColumnView> arguments) {
        ColumnView x = arguments.get(0);
        ColumnView y = arguments.get(1);

        double result[] = new double[x.numRows()];
        for (int i = 0; i < result.length; i++) {
            double xd = x.getDouble(i);
            double yd = y.getDouble(i);
            if(Double.isNaN(xd) && Double.isNaN(yd)) {
                result[i] = Double.NaN;
            } else {
                if (Double.isNaN(xd)) {
                    xd = 0;
                }
                if( Double.isNaN(yd)) {
                    yd = 0;
                }
                result[i] = apply(xd, yd);
            }
        }
        return new DoubleArrayColumnView(result);
    }

    @Override
    public final String getId() {
        return name;
    }

    @Override
    public final String getLabel() {
        return name;
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        //must be unary or binary function
        if(argumentTypes.size() == 1) {

            if(argumentTypes.get(0) instanceof QuantityType) {
                QuantityType t = (QuantityType) argumentTypes.get(0);
                return new QuantityType().setUnits(t.getUnits());
            } else {
                throw new InvalidTypeException("Not Real Valued Argument");
            }

        } else if(argumentTypes.size() == 2) {

            if(argumentTypes.get(0) instanceof QuantityType &&
                    argumentTypes.get(1) instanceof QuantityType) {

                QuantityType t1 = (QuantityType) argumentTypes.get(0);
                QuantityType t2 = (QuantityType) argumentTypes.get(1);

                return new QuantityType().setUnits(applyUnits(t1.getUnits(), t2.getUnits()));

            } else {
                throw new InvalidTypeException("Cannot compare types " +
                        argumentTypes.get(0).getTypeClass().getId() + " and " +
                        argumentTypes.get(1).getTypeClass().getId());
            }

        } else {
            throw new FormulaSyntaxException("The " + getLabel() + "() function expects exactly 1 or 2 argument(s).");
        }

    }

    protected abstract double apply(double a);

    protected abstract double apply(double a, double b);

    protected abstract String applyUnits(String a, String b);
}
