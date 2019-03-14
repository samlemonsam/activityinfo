/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    private Tab get(LocalDate day) {
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

