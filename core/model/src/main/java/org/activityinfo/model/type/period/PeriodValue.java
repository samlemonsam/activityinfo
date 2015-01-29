package org.activityinfo.model.type.period;
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

import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

import java.io.Serializable;

/**
 * @author yuriyz on 01/27/2015.
 */
public class PeriodValue implements FieldValue, IsRecord, Serializable {

    private int year;
    private int month;
    private int week;
    private int day;
    private int hour;
    private int minute;
    private int second;

    public PeriodValue() {
    }

    public PeriodValue(int year, int month, int week, int day, int hour, int minute, int second) {
        this.year = year;
        this.month = month;
        this.week = week;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public static FieldValue fromRecord(Record record) {
        return Iso8601.parse(record.getString("period"));
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return PeriodType.TYPE_CLASS;
    }

    @Override
    public Record asRecord() {
        return new Record()
                .set(TYPE_CLASS_FIELD_NAME, getTypeClass().getId())
                .set("period", Iso8601.asIso8601String(this));
    }

    public int getYear() {
        return year;
    }

    public PeriodValue setYear(int year) {
        this.year = year;
        return this;
    }

    public int getMonth() {
        return month;
    }

    public PeriodValue setMonth(int month) {
        this.month = month;
        return this;
    }

    public int getWeek() {
        return week;
    }

    public PeriodValue setWeek(int week) {
        this.week = week;
        return this;
    }

    public int getDay() {
        return day;
    }

    public PeriodValue setDay(int day) {
        this.day = day;
        return this;
    }

    public int getHour() {
        return hour;
    }

    public PeriodValue setHour(int hour) {
        this.hour = hour;
        return this;
    }

    public int getMinute() {
        return minute;
    }

    public PeriodValue setMinute(int minute) {
        this.minute = minute;
        return this;
    }

    public int getSecond() {
        return second;
    }

    public PeriodValue setSecond(int second) {
        this.second = second;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeriodValue period = (PeriodValue) o;

        if (day != period.day) return false;
        if (hour != period.hour) return false;
        if (minute != period.minute) return false;
        if (month != period.month) return false;
        if (second != period.second) return false;
        if (week != period.week) return false;
        if (year != period.year) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = year;
        result = 31 * result + month;
        result = 31 * result + week;
        result = 31 * result + day;
        result = 31 * result + hour;
        result = 31 * result + minute;
        result = 31 * result + second;
        return result;
    }

    @Override
    public String toString() {
        return "Period{" +
                "year=" + year +
                ", month=" + month +
                ", week=" + week +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                '}';
    }

    public boolean isZero() {
        return year == 0 && month == 0 && week == 0 && day == 0 && hour == 0 && minute == 0 && second == 0;
    }
}
