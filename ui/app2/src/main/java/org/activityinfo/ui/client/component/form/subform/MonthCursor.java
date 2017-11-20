package org.activityinfo.ui.client.component.form.subform;


import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.type.time.Month;

import java.util.Date;

public class MonthCursor implements PeriodCursor<Month> {

    private Month currentMonth;

    public MonthCursor() {
        currentMonth = Month.of(new Date());
    }

    @Override
    public Tab get(int i) {
        return get(currentMonth.plus(i));
    }

    public Tab get(Month month) {
        return new Tab(month.toString(), month.toString(), SubFormKind.MONTHLY);
    }

    public Tab get(String dataPeriod) {
        return get(getValue(dataPeriod));
    }

    @Override
    public void advance(int count) {
        currentMonth = currentMonth.plus(count);
    }

    @Override
    public Month getValue(String dataPeriod) {
        return Month.parseMonth(dataPeriod);
    }

    @Override
    public Month getCurrentPeriod() {
        return currentMonth;
    }

    public void setCurrentPeriod(Month currentMonth) {
        this.currentMonth = currentMonth;
    }
}
