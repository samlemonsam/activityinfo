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
