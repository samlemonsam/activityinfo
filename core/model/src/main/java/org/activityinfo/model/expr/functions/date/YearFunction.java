package org.activityinfo.model.expr.functions.date;

import org.activityinfo.model.type.time.LocalDate;


public class YearFunction extends DateComponentFunction {

    public static final YearFunction INSTANCE = new YearFunction();

    private YearFunction() {}

    @Override
    public String getId() {
        return "year";
    }

    @Override
    public String getLabel() {
        return "YEAR";
    }

    @Override
    protected String getUnits() {
        return "years";
    }

    @Override
    protected int apply(LocalDate date) {
        return date.getYear();
    }

}
