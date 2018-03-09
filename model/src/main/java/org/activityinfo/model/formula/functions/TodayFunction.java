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
import org.activityinfo.model.query.ConstantColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.Date;
import java.util.List;

/**
 * Returns today's current date as {@link org.activityinfo.model.type.time.LocalDate} value
 */
public class TodayFunction extends FormulaFunction implements ColumnFunction {

    public static final TodayFunction INSTANCE = new TodayFunction();

    private TodayFunction() {}

    @Override
    public String getId() {
        return "today";
    }

    @Override
    public String getLabel() {
        return "today";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        if(arguments.size() != 0) {
            throw new FormulaSyntaxException("The TODAY() function doesn't take any arguments");
        }
        return new LocalDate(new Date());
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return LocalDateType.INSTANCE;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        return new ConstantColumnView(numRows, new LocalDate(new Date()).toString());
    }
}
