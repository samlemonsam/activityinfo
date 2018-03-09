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

import org.activityinfo.model.type.FieldValue;

/**
 * @author yuriyz on 10/08/2015.
 */
public class GreaterOrEqualFunction extends ComparisonOperator {

    public static final GreaterOrEqualFunction INSTANCE = new GreaterOrEqualFunction();

    public GreaterOrEqualFunction() {
        super(">=");
    }

    public String getLabel() {
        return "Greater or equal";
    }

    @Override
    protected boolean apply(FieldValue a, FieldValue b) {
        Double da = extractDouble(a);
        Double db = extractDouble(b);

        if (da != null && db != null) {
            return da >= db;
        }

        throw new UnsupportedOperationException("Operators are not supported.");
    }

    @Override
    protected boolean apply(double x, double y) {
        return x >= y;
    }
}
