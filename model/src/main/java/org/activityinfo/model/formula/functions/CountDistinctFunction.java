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

import java.util.Arrays;

public class CountDistinctFunction extends StatFunction {

    public static final CountDistinctFunction INSTANCE = new CountDistinctFunction();

    private CountDistinctFunction() {}

    @Override
    public String getId() {
        return "countdistinct";
    }

    @Override
    public String getLabel() {
        return "Count Distinct";
    }

    @Override
    public double compute(double[] values, int start, int end) {
        Arrays.sort(values, start, end);
        int count = 0;
        double lastValue = Double.NaN;
        for (int i = start; i < end; i++) {
            double value = values[i];
            if(!Double.isNaN(value)) {
                if(value != lastValue) {
                    count++;
                }
                lastValue = value;
            }
        }
        return count;
    }

    @Override
    public double emptyValue() {
        return 0;
    }
}
