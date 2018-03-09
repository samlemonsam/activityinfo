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

import org.activityinfo.model.type.number.QuantityType;

import java.util.Objects;

class MinusFunction extends RealValuedFunction {

    public MinusFunction() {
        super("-");
    }

    @Override
    protected double apply(double a) {
        return -a;
    }

    @Override
    protected double apply(double a, double b) {
        return a - b;
    }

    @Override
    protected String applyUnits(String a, String b) {
        if(Objects.equals(a, b)) {
            return a;
        } else {
            return QuantityType.UNKNOWN_UNITS;
        }
    }
}
