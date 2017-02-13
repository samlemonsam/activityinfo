package org.activityinfo.model.expr.functions.date;

import org.activityinfo.model.type.time.LocalDate;

/**
 * Returns the month of year of the given date.
 */
public class MonthFunction extends DateComponentFunction {
    @Override
    public String getId() {
        return "month";
    }

    @Override
    public String getLabel() {
        return "Month";
    }

    @Override
    protected String getUnits() {
        return "months";
    }

    @Override
    protected int apply(LocalDate date) {
        return date.getMonthOfYear();
    }
}
