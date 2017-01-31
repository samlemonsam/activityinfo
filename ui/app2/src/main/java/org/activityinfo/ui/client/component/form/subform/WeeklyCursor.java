package org.activityinfo.ui.client.component.form.subform;

import org.activityinfo.model.date.EpiWeek;
import org.activityinfo.model.date.Month;
import org.activityinfo.model.form.SubFormKind;

import java.util.Date;

/**
 * Created by yuriyz on 8/12/2016.
 */
public class WeeklyCursor implements PeriodCursor<EpiWeek> {

    private EpiWeek currentWeek;

    public WeeklyCursor() {
        Month month = Month.of(new Date());
        currentWeek = new EpiWeek(month.getMonth() * 4, month.getYear());
    }

    @Override
    public Tab get(int i) {
        return get(currentWeek.plus(i));
    }

    public Tab get(EpiWeek week) {
        return new Tab(week.toString(), week.toString(), SubFormKind.WEEKLY);
    }

    public Tab get(String dataPeriod) {
        return get(getValue(dataPeriod));
    }

    @Override
    public void advance(int count) {
        currentWeek = currentWeek.plus(count);
    }

    @Override
    public EpiWeek getValue(String dataPeriod) {
        return EpiWeek.parse(dataPeriod);
    }

    @Override
    public EpiWeek getCurrentPeriod() {
        return currentWeek;
    }

    public void setCurrentPeriod(EpiWeek currentWeek) {
        this.currentWeek = currentWeek;
    }
}

