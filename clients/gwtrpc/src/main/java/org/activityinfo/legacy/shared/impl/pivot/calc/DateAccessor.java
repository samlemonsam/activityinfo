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
package org.activityinfo.legacy.shared.impl.pivot.calc;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.content.*;
import org.activityinfo.legacy.shared.reports.model.DateDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.date.DateUnit;

public class DateAccessor implements DimAccessor {

    private DateUnit dateUnit;
    private DateDimension dateDim;

    public DateAccessor(DateDimension dateDim) {
        this.dateDim = dateDim;
        this.dateUnit = dateDim.getUnit();
    }


    @Override
    public Dimension getDimension() {
        return dateDim;
    }

    @Override
    public DimensionCategory getCategory(SiteDTO siteDTO) {
        LocalDate date = siteDTO.getDate2();
        if(date == null) {
            return null;
        }
        switch(dateUnit) {
            case YEAR:
                return new YearCategory(date.getYear());
            case QUARTER:
                return new QuarterCategory(date.getYear(), quarterFromMonth(date.getMonthOfYear()));
            case MONTH:
                return new MonthCategory(date.getYear(), date.getMonthOfYear());
            case WEEK_MON:
                // TODO(Alex)
                return null;
            case DAY:
                return new DayCategory(date.atMidnightInMyTimezone());
        }
        return new MonthCategory(date.getYear(), date.getMonthOfYear());
    }

    private int quarterFromMonth(int monthOfYear) {
        if(monthOfYear <= 3) {
            return 1;
        } else if(monthOfYear <= 6) {
            return 2;
        } else if(monthOfYear <= 9) {
            return 3;
        } else {
            return 4;
        }
    }
}
