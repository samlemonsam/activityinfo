package org.activityinfo.model.form;


import org.activityinfo.model.type.time.*;

public enum SubFormKind {
    REPEATING(null),
    MONTHLY(MonthType.INSTANCE),
    WEEKLY(EpiWeekType.INSTANCE),
    BIWEEKLY(FortnightType.INSTANCE),
    DAILY(LocalDateType.INSTANCE);

    private PeriodType periodType;

    SubFormKind(PeriodType periodType) {
        this.periodType = periodType;
    }

    public PeriodType getPeriodType() {
        assert periodType != null;
        return periodType;
    }

    public boolean isPeriod() {
        return periodType != null;
    }
}
