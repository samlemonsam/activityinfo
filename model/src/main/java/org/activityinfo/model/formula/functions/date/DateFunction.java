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
import org.activityinfo.model.formula.functions.Casting;
import org.activityinfo.model.formula.functions.ColumnFunction;
import org.activityinfo.model.formula.functions.FormulaFunction;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.List;

/**
 * DATE() function implementation.
 *
 * <p>Can you be used to create a LocalDate value, for example: DATE(2015,1,1)</p>
 */
public class DateFunction extends FormulaFunction implements ColumnFunction {

    public static final DateFunction INSTANCE = new DateFunction();

    private DateFunction() {
    }

    @Override
    public String getId() {
        return "DATE";
    }

    @Override
    public String getLabel() {
        return "DATE";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        if(arguments.size() != 3) {
            throw new FormulaSyntaxException("DATE() expects three arguments");
        }
        return apply(arguments.get(0), arguments.get(1), arguments.get(2));
    }

    public static FieldValue apply(FieldValue yearArgument, FieldValue monthArgument, FieldValue dayArgument) {
        double year = Casting.toQuantity(yearArgument).getValue();
        double month = Casting.toQuantity(monthArgument).getValue();
        double day = Casting.toQuantity(dayArgument).getValue();

        return apply(year, month, day);
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return LocalDateType.INSTANCE;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        ColumnView year = arguments.get(0);
        ColumnView month = arguments.get(1);
        ColumnView day = arguments.get(2);

        String[] dates = new String[numRows];
        for (int i = 0; i < numRows; i++) {
            dates[i] = apply(
                year.getDouble(i),
                month.getDouble(i),
                day.getDouble(i)).toString();
        }

        return new StringArrayColumnView(dates);
    }



    private static LocalDate apply(double year, double month, double day) {
        return new LocalDate((int)year, (int)month, (int)day);
    }
}
