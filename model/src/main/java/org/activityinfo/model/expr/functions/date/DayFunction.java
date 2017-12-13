package org.activityinfo.model.expr.functions.date;

import org.activityinfo.model.type.time.LocalDate;


public class DayFunction extends DateComponentFunction {

    public static final DayFunction INSTANCE = new DayFunction();

    private DayFunction() {}

    @Override
    public String getId() {
        return "day";
    }

    @Override
    public String getLabel() {
        return "day";
    }

    @Override
    protected String getUnits() {
        return "days";
    }

    @Override
    protected int apply(LocalDate date) {
        return date.getDayOfMonth();
    }
}
