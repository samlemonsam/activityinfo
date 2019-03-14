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

/**
 * A cursor over a sequence of periods. 
 */
public interface PeriodCursor<T> {

    /**
     * Gets the {@code i}-th period, relative to the current position.
     */
    Tab get(int i);

    Tab get(String dataPeriod);

    /**
     * Advances (or retreats) the cursor {@code count} periods relative to the current
     * position.
     */
    void advance(int count);

    T getValue(String dataPeriod);

    T getCurrentPeriod();

    void setCurrentPeriod(T period);
}
