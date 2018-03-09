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

public class BooleanFunctions {

    public static final FormulaFunction AND = AndFunction.INSTANCE;
    public static final FormulaFunction OR = OrFunction.INSTANCE;
    public static final ComparisonOperator EQUAL = EqualFunction.INSTANCE;
    public static final ComparisonOperator NOT_EQUAL = NotEqualFunction.INSTANCE;

    public static final ComparisonOperator GREATER = GreaterFunction.INSTANCE;
    public static final ComparisonOperator GREATER_OR_EQUAL = GreaterOrEqualFunction.INSTANCE;
    public static final ComparisonOperator LESS = LessFunction.INSTANCE;
    public static final ComparisonOperator LESS_OR_EQUAL = LessOrEqualFunction.INSTANCE;

}
