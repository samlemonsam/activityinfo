package org.activityinfo.ui.client.component.form.subform;


import org.activityinfo.legacy.shared.command.Month;

import java.util.Date;

public class MonthCursor implements PeriodCursor {

    private Month currentMonth;

    public MonthCursor() {
        currentMonth = Month.of(new Date());
    }

    @Override
    public PeriodTab get(int i) {
        Month month = currentMonth.plus(i);
        return new PeriodTab(month.toString(), month.toString());
    }

    @Override
    public void advance(int count) {
        currentMonth = currentMonth.plus(count);
    }
}
