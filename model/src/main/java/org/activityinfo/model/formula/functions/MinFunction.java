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

/**
 * Compute the minimum value of its arguments
 */
public class MinFunction extends StatFunction {
    
    public static final MinFunction INSTANCE = new MinFunction();
    
    private MinFunction() {}
    
    @Override
    public String getId() {
        return "min";
    }

    @Override
    public String getLabel() {
        return "min";
    }


    @Override
    public double compute(double[] values, int start, int end) {
        double min = Double.NaN;
        for(int i=start;i<end;++i) {
            double value = values[i];
            if(Double.isNaN(min)) {
                min = value;
            } else if(!Double.isNaN(value)) {
                if(value < min) {
                    min = value;
                }
            }
        }
        return min;    
    }
}
