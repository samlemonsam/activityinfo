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

import org.activityinfo.model.formula.functions.ColumnFunction;
import org.activityinfo.model.formula.functions.FormulaFunction;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.List;

public class AddDateFunction extends FormulaFunction implements ColumnFunction {

    public static final AddDateFunction INSTANCE = new AddDateFunction();

    private AddDateFunction() {
    }

    @Override
    public String getId() {
        return "adddate";
    }

    @Override
    public String getLabel() {
        return "adddate";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        LocalDate date = (LocalDate) arguments.get(0);
        Quantity days = (Quantity) arguments.get(1);

        return date.plusDays((int) days.getValue());
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return LocalDateType.INSTANCE;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        ColumnView dateView = arguments.get(0);
        ColumnView daysView = arguments.get(1);
        String[] result = new String[dateView.numRows()];
        for (int i = 0; i < dateView.numRows(); i++) {
            LocalDate date = LocalDate.parse(dateView.getString(i));
            int days = (int)daysView.getDouble(i);
            result[i] = date.plusDays(days).toString();
        }
        return new StringArrayColumnView(result);
    }
}
