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
import org.activityinfo.model.query.BooleanColumnView;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.List;

public abstract class BinaryBooleanOperator extends FormulaFunction implements ColumnFunction {

    private final String name;

    protected BinaryBooleanOperator(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public BooleanFieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments,2);
        boolean a = Casting.toBoolean(arguments.get(0));
        boolean b = Casting.toBoolean(arguments.get(1));

        return BooleanFieldValue.valueOf(apply(a, b));
    }

    @Override
    public final boolean isInfix() {
        return true;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        checkArity(arguments,2);

        ColumnView a = arguments.get(0);
        ColumnView b = arguments.get(1);

        if(a.numRows() != b.numRows()) {
            throw new FormulaSyntaxException("Arguments must have the same number of rows");
        }

        int[] result = new int[a.numRows()];
        for (int i = 0; i < result.length; i++) {
            int ai = a.getBoolean(i);
            int bi = b.getBoolean(i);
            result[i] = apply(ai, bi);
        }
        return new BooleanColumnView(result);
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        if(argumentTypes.size() != 2) {
            throw new FormulaSyntaxException("Expected two arguments");
        }
        if(!(argumentTypes.get(0) instanceof BooleanType)) {
            throw new ArgumentException(0, "Expected TRUE/FALSE value");
        }
        if(!(argumentTypes.get(1) instanceof BooleanType)) {
            throw new ArgumentException(1, "Expected TRUE/FALSE value");
        }
        return BooleanType.INSTANCE;
    }

    public abstract boolean apply(boolean a, boolean b);

    /**
     * Apply the function to {@code a} and {@code b}, which must have the values 
     * {@link ColumnView#TRUE}, {@link ColumnView#FALSE} or {@link ColumnView#NA}
     */
    public abstract int apply(int a, int b);

}
