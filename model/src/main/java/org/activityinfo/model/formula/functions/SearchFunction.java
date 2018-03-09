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

import com.google.common.base.Strings;
import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.ConstantColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

import java.util.List;

/**
 * Returns the location of a substring in a string. The search is NOT case-sensitive.
 *
 * <p>If no match is found, the result is NaN</p>
 *
 * <p>The return index as well as the search index is 1-based.</p>
 */
public class SearchFunction extends FormulaFunction implements ColumnFunction {

    public static final SearchFunction INSTANCE = new SearchFunction();

    private SearchFunction() {
    }

    @Override
    public String getId() {
        return "SEARCH";
    }

    @Override
    public String getLabel() {
        return "SEARCH";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments.size());

        String substring = Casting.toString(arguments.get(0));
        String string = Casting.toString(arguments.get(1));
        double startPosition = 1;
        if(arguments.size() == 3) {
            startPosition = Casting.toQuantity(arguments.get(2)).getValue();
        }

        return new Quantity(apply(substring, string, startPosition));
    }

    private double apply(String substring, String string, double startPositionArgument) {

        if(Double.isNaN(startPositionArgument) || Double.isInfinite(startPositionArgument) ||
            startPositionArgument > Integer.MAX_VALUE) {
            return Double.NaN;
        }

        if(Strings.isNullOrEmpty(substring) || Strings.isNullOrEmpty(string)) {
            return Double.NaN;
        }

        int startIndex = (int)startPositionArgument - 1;

        int index = string.toLowerCase().indexOf(substring.toLowerCase(), startIndex);
        if(index == -1) {
            return Double.NaN;
        } else {
            return index + 1;
        }
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        if(argumentTypes.size() != 1) {
            throw new FormulaSyntaxException("Expected single argument");
        }
        return new QuantityType();
    }


    private void checkArity(int count) {
        if(count < 2 || count > 3) {
            throw new FormulaSyntaxException("SEARCH() expects two or three arguments.");
        }
    }


    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        ColumnView substring = arguments.get(0);
        ColumnView string = arguments.get(1);
        ColumnView startIndex;
        if(arguments.size() == 3) {
            startIndex = arguments.get(3);
        } else {
            startIndex = new ConstantColumnView(numRows, 1d);
        }

        double[] result = new double[numRows];
        for (int i = 0; i < numRows; i++) {
            result[i] = apply(
                substring.getString(i),
                string.getString(i),
                startIndex.getDouble(i));
        }

        return new DoubleArrayColumnView(result);
    }
}
