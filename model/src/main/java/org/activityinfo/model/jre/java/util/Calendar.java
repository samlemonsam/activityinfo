package java.util;

import java.util.Date;

/**
 * Dummy calendar for super-source
 */
public class Calendar {

    public final static int DAY_OF_MONTH = 5;
    public final static int DAY_OF_YEAR = 6;
    public final static int DAY_OF_WEEK = 7;

    public Calendar() {
    }

    public static Calendar getInstance() {
        throw new UnsupportedOperationException();
    }

    public void setTime(Date date) {
        throw new UnsupportedOperationException();
    }

    public void add(int field, int amount) {
        throw new UnsupportedOperationException();
    }

    public final void set(int year, int month, int date) {
        throw new UnsupportedOperationException();
    }

    public final Date getTime() {
        throw new UnsupportedOperationException();
    }

    public int get(int field) {
        throw new UnsupportedOperationException();
    }

    public int getActualMaximum(int field) {
        throw new UnsupportedOperationException();
    }
}