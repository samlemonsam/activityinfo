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
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

import java.util.List;

public abstract class UnaryFunctionBase extends FormulaFunction implements ColumnFunction {

    private final String name;

    private static int MAXARGS = 1;

    protected UnaryFunctionBase(String name) { this.name = name; }

    @Override
    public String getId() { return name; }

    @Override
    public String getLabel() { return name; }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments,MAXARGS);
        FieldValue unaryArg = arguments.get(0);
        return apply(unaryArg);
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        checkArity(arguments,MAXARGS);
        ColumnView unaryArgument = arguments.get(0);
        return columnApply(numRows,unaryArgument);
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        if(argumentTypes.size() != 1) {
            throw new FormulaSyntaxException("Expected single argument");
        }
        FieldType unaryArgType = argumentTypes.get(0);
        return resolveUnaryResultType(unaryArgType);
    }

    public abstract FieldType resolveUnaryResultType(FieldType argumentType);

    /**
     * Apply the function to a single {@code argument}
     */
    public abstract FieldValue apply(FieldValue argument);

    public abstract ColumnView columnApply(int numRows, ColumnView argument);

}
