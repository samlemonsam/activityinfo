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
package org.activityinfo.test.capacity.load;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import org.joda.time.Duration;
import org.joda.time.Period;

import javax.annotation.Nonnull;

/**
 * Function of time that uses a logistic growth function for a ramp up period
 * followed by a steady state.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Logistic_function">Logistic Function (Wikipedia)</a>
 */
public class LogisticGrowthFunction implements Function<Duration, Integer> {

    private int maxValue = 10;
    private double rampUpDuration = 1000;

    public LogisticGrowthFunction during(Period period) {
        this.rampUpDuration = period.toStandardDuration().getMillis();
        return this;
    }

    @Override
    public Integer apply(@Nonnull Duration input) {
        return apply(input.getMillis() / rampUpDuration);
    }

    @VisibleForTesting
    int apply(double proportionElapsed) {

        // scale x into the range [-6, 6]
        double x = (proportionElapsed * 12d) - 6d;
        double y = 1d / (1d + Math.exp(-x));

        // scale y to our max value
        return (int)Math.round( y * ((double)maxValue));
    }

    public static LogisticGrowthFunction rampUpTo(int maxValue) {
        LogisticGrowthFunction f = new LogisticGrowthFunction();
        f.maxValue = maxValue;
        return f;
    }
}