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
package org.activityinfo.model.date;

import org.activityinfo.model.type.time.EpiWeek;

/**
 * Created by yuriyz on 8/15/2016.
 */
public class BiWeek {

    private EpiWeek startWeek;

    public BiWeek(EpiWeek startWeek) {
        this.startWeek = startWeek;
    }

    public BiWeek(int startWeekInYear, int year) {
        this(new EpiWeek(year, startWeekInYear));
    }

    public EpiWeek getStartWeek() {
        return startWeek;
    }

    public EpiWeek getEndWeek() {
        return new EpiWeek(startWeek.next());
    }

    public String toString() {
        return startWeek.toString() + "-" + getEndWeek().getWeekInYear();
    }

    /**
     * @param biweek string representation ( e.g. 2016W1-2)
     * @return biweek object
     */
    public static BiWeek parse(String biweek) {
        String[] parts = biweek.split("W");
        String[] subParts = parts[1].split("-");
        return new BiWeek(Integer.parseInt(subParts[0]), Integer.parseInt(parts[0]));
    }

    public BiWeek plus(int count) {
        return new BiWeek(startWeek.plus(2 * count));
    }

    public BiWeek next() {
        return plus(+1);
    }

    public BiWeek previous() {
        return plus(-1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BiWeek biWeek = (BiWeek) o;

        return !(startWeek != null ? !startWeek.equals(biWeek.startWeek) : biWeek.startWeek != null);

    }

    @Override
    public int hashCode() {
        return startWeek != null ? startWeek.hashCode() : 0;
    }
}
