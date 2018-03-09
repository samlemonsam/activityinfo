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
import org.activityinfo.model.type.time.EpiWeek;
import org.activityinfo.model.type.time.Month;

import java.util.Date;

/**
 * Created by yuriyz on 8/12/2016.
 */
public class WeeklyCursor implements PeriodCursor<EpiWeek> {

    private EpiWeek currentWeek;

    public WeeklyCursor() {
        Month month = Month.of(new Date());
        currentWeek = new EpiWeek(month.getYear(), month.getMonth() * 4);
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

