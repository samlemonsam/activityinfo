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
package org.activityinfo.test.driver;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;


public class ApiErrorRate implements Metric {
    
    
    private Meter commands = new Meter();
    private Meter errors = new Meter();

    public void markSubmitted() {
        commands.mark();
    }
    
    public void markError() {
        errors.getCount();
    }
    
    public long getTotalErrorCount() {
        return errors.getCount();
    }
    
    public double getTotalErrorRate() {
        double numerator =  errors.getCount();
        double denominator = commands.getCount();
        return numerator / denominator;
    }
    
    public double getOneMinuteRate() {
        double numerator =  errors.getOneMinuteRate();
        double denominator = commands.getOneMinuteRate();
        return numerator / denominator;
    }
    
}
