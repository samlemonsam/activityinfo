package org.activityinfo.legacy.shared.reports.content;


import com.bedatadriven.rebar.time.calendar.LocalDate;

import java.util.Date;

public class DayCategory implements DimensionCategory {
    private LocalDate date;

    public DayCategory() {
    }

    public DayCategory(Date date) {
        this.date = new LocalDate(date);
    }

    @Override
    public Comparable getSortKey() {
        return date;
    }

    @Override
    public String getLabel() {
        return date.toString();
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
