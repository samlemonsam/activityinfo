package org.activityinfo.model.date;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.i18n.shared.DateTimeFormat;

import java.util.Date;

/**
 * @author yuriyz on 07/03/2015.
 */
public enum DayOfWeek {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6);
    private int value;

    DayOfWeek(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DayOfWeek fromValue(int value) {
        for (DayOfWeek dayOfWeek : values()) {
            if (dayOfWeek.getValue() == value) {
                return dayOfWeek;
            }
        }
        throw new IllegalArgumentException("Value must be between 0..6, value: " + value);
    }

    public static DayOfWeek dayOfWeek(Date date) {
        return DayOfWeek.fromValue(Integer.parseInt(DateTimeFormat.getFormat("c").format(date)));
    }

}
