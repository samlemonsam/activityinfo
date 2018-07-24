package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.type.time.EpiWeek;
import org.activityinfo.model.type.time.FortnightValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.Month;

public class DateEncoding {


    /**
     * Encode a year-month value as a 4-byte integer, with the year in the most significant
     * word, and the month in the least significant word.
     */
    public static int encodeMonth(Month month) {
       return encodeYearPart(month.getYear(), month.getMonth());
    }

    private static int encodeYearPart(int year, int yearPart) {
        return year << 16 | yearPart;
    }

    public static Month decodeMonth(int monthInt) {
        return new Month(getYear(monthInt), getYearPart(monthInt));
    }

    public static int getYear(int monthInt) {
        return monthInt >> 16;
    }

    public static int getYearPart(int monthInt) {
        return monthInt & 0xFFFF;
    }

    public static int encodeFortnight(FortnightValue value) {
        return encodeYearPart(value.getYear(), value.getWeekInYear());
    }

    public static int encodeWeek(EpiWeek week) {
        return encodeYearPart(week.getYear(), week.getWeekInYear());
    }

    public static int encodeLocalDate(LocalDate value) {
        return value.getYear() << 16 | value.getMonthOfYear() << 8 | value.getDayOfMonth();
    }

    public static int getLocalDateMonth(int localDate) {
        return (localDate & 0xFF00) >> 8;
    }

    public static int getLocalDateDay(int localDate) {
        return localDate & 0xFF;
    }

    public static LocalDate decodeLocalDate(int localDate) {
        return new LocalDate(getYear(localDate), getLocalDateMonth(localDate), getLocalDateDay(localDate));
    }


}
