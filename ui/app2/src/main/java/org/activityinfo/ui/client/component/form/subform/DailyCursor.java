package org.activityinfo.ui.client.component.form.subform;

import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Date;

/**
 * Created by yuriyz on 8/11/2016.
 */
public class DailyCursor implements PeriodCursor<LocalDate> {

    private LocalDate currentDay;

    public DailyCursor() {
        currentDay = new LocalDate(new Date());
    }

    @Override
    public Tab get(int i) {
        return get(currentDay.plusDays(i));
    }

    public Tab get(LocalDate day) {
        return new Tab(day.toString(), day.toString(), SubFormKind.DAILY);
    }

    public Tab get(String dataPeriod) {
        return get(getValue(dataPeriod));
    }

    @Override
    public void advance(int count) {
        currentDay = currentDay.plusDays(count);
    }

    @Override
    public LocalDate getValue(String dataPeriod) {
        return LocalDate.parse(dataPeriod);
    }

    @Override
    public LocalDate getCurrentPeriod() {
        return currentDay;
    }

    public void setCurrentPeriod(LocalDate currentDay) {
        this.currentDay = currentDay;
    }
}

