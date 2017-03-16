package org.activityinfo.model.expr.functions.date;

import org.activityinfo.model.type.time.LocalDate;

/**
 * Returns the month of year of the given date.
 */
public class MonthFunction extends DateComponentFunction {

    public static final MonthFunction INSTANCE = new MonthFunction();

    private MonthFunction() {}

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

    public static int fromIsoString(String string) {
        return Integer.parseInt(string.substring(5, 7));
    }
}
