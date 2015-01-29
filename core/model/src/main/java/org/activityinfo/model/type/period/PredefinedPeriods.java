package org.activityinfo.model.type.period;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

/**
 * @author yuriyz on 01/27/2015.
 */
public enum PredefinedPeriods {

    YEARLY(new PeriodValue().setYear(1)),
    MONTHLY(new PeriodValue().setMonth(1)),
    BI_WEEKLY(new PeriodValue().setWeek(2)),
    WEEKLY(new PeriodValue().setWeek(1)),
    DAILY(new PeriodValue().setDay(1)),
    HOURLY(new PeriodValue().setHour(1));

    private final PeriodValue period;
    private String label;

    PredefinedPeriods(PeriodValue period) {
        this.period = period;
    }

    public PeriodValue getPeriod() {
        return period;
    }

    public String getLabel() {
        return label;
    }
}
