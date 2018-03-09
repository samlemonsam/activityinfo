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
