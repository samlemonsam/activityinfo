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

public class DivideFunction extends RealValuedFunction {

    public static final DivideFunction INSTANCE = new DivideFunction();

    private DivideFunction() {
        super("/");
    }

    @Override
    protected double apply(double a) {
        throw new IllegalStateException("Illegal unary input to " + getLabel() + "()");
    }

    @Override
    protected double apply(double a, double b) {
        if(b == 0) {
            return Double.NaN;
        }
        return a / b;
    }

    @Override
    protected String applyUnits(String a, String b) {
        // TODO: we need to properly model units in order to handle this
        return "(" + a + ")/(" + b + ")";
    }
}
