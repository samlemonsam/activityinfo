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
package org.activityinfo.ui.client.input.view.field;

import org.activityinfo.model.type.time.EpiWeek;
import org.activityinfo.model.type.time.EpiWeekType;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.ArrayList;
import java.util.List;

public class WeekWidget extends AbstractWeekWidget<EpiWeek> {


    public WeekWidget(FieldUpdater updater) {
        super(EpiWeekType.INSTANCE, updater);
    }

    @Override
    protected List<String> periodList() {
        List<String> weeks = new ArrayList<>();
        for (int i = 1; i <= EpiWeek.WEEKS_IN_YEAR; i++) {
            weeks.add("W" + i);
        }
        return weeks;
    }

    @Override
    protected String yearName(EpiWeek period) {
        return Integer.toString(period.getYear());
    }

    @Override
    protected String periodName(EpiWeek period) {
        return "W" + period.getWeekInYear();
    }

    @Override
    protected FieldInput parseInput(int year, int periodIndex) {
        return new FieldInput(new EpiWeek(year, periodIndex + 1));
    }
}
