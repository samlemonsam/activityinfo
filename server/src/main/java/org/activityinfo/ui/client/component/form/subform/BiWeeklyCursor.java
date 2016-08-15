package org.activityinfo.ui.client.component.form.subform;

import org.activityinfo.model.date.BiWeek;
import org.activityinfo.model.date.Month;
import org.activityinfo.model.form.SubFormKind;

import java.util.Date;

/**
 * Created by yuriyz on 8/15/2016.
 */
public class BiWeeklyCursor implements PeriodCursor<BiWeek> {

    private BiWeek currentWeek;

    public BiWeeklyCursor() {
        Month month = Month.of(new Date());
        currentWeek = new BiWeek(month.getMonth() * 4, month.getYear());
    }

    @Override
    public Tab get(int i) {
        return get(currentWeek.plus(i));
    }

    public Tab get(BiWeek week) {
        return new Tab(week.toString(), week.toString(), SubFormKind.BIWEEKLY);
    }

    public Tab get(String dataPeriod) {
        return get(getValue(dataPeriod));
    }

    @Override
    public void advance(int count) {
        currentWeek = currentWeek.plus(count);
    }

    @Override
    public BiWeek getValue(String dataPeriod) {
        return BiWeek.parse(dataPeriod);
    }

    @Override
    public BiWeek getCurrentPeriod() {
        return currentWeek;
    }

    public void setCurrentPeriod(BiWeek currentWeek) {
        this.currentWeek = currentWeek;
    }
}