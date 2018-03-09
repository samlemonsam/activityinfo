package org.activityinfo.model.formula.functions.date;


import org.activityinfo.model.type.time.LocalDate;

public class QuarterFunction extends DateComponentFunction {

    public static final QuarterFunction INSTANCE = new QuarterFunction();

    private QuarterFunction() {}

    @Override
    public String getId() {
        return "quarter";
    }

    @Override
    public String getLabel() {
        return "quarter";
    }

    @Override
    protected String getUnits() {
        return "quarters";
    }

    @Override
    protected int apply(LocalDate date) {
        return fromMonth(date.getMonthOfYear());
    }

    public static int fromMonth(int month) {
        assert month >= 1 && month <= 12;
        return 1 + ( (month-1) / 3);
    }
}
