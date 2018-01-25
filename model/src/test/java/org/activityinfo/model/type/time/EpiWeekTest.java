package org.activityinfo.model.type.time;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class EpiWeekTest {

    @Test
    public void serialization() {
        EpiWeek epiWeek = new EpiWeek(2016, 3);
        EpiWeek deserialized = EpiWeek.parse(epiWeek.toString());

        assertEquals(epiWeek, deserialized);
    }

    @Test
    public void plus() {
        EpiWeek epiWeek = new EpiWeek(2016, 3);

        assertEquals(epiWeek.plus(1), new EpiWeek(2016, 4));
        assertEquals(epiWeek.plus(10), new EpiWeek(2016, 13));
        assertEquals(epiWeek.plus(52), new EpiWeek(2017, 3));
    }

    @Test
    public void dateRange() {
        assertDateRange(2013, 1, new LocalDate(2012, 12, 30), new LocalDate(2013, 1, 5));
        assertDateRange(2013, 9, new LocalDate(2013, 2, 24), new LocalDate(2013, 3, 2));
        assertDateRange(2013, 27, new LocalDate(2013, 6, 30), new LocalDate(2013, 7, 6));
        assertDateRange(2013, 52, new LocalDate(2013, 12, 22), new LocalDate(2013, 12, 28));

        // http://www.cmmcp.org/2012EPI.htm
        assertDateRange(2012, 1, new LocalDate(2012, 1, 1), new LocalDate(2012, 1, 7));
        assertDateRange(2012, 23, new LocalDate(2012, 6, 3), new LocalDate(2012, 6, 9));

        // http://www.cmmcp.org/2016EPI.htm
        assertDateRange(2016, 1, new LocalDate(2016, 1, 3), new LocalDate(2016, 1, 9));
        assertDateRange(2016, 13, new LocalDate(2016, 3, 27), new LocalDate(2016, 4, 2));
        assertDateRange(2016, 52, new LocalDate(2016, 12, 25), new LocalDate(2016, 12, 31));
    }

    private static void assertDateRange(int year, int weekInYear, LocalDate startDate, LocalDate endDate) {
        assertEquals(new LocalDateInterval(startDate, endDate), new EpiWeek(year, weekInYear).asInterval());
    }


    @Test
    public void firstDayOfEpicWeekInYear() {
        assertFirstDayOfEpicWeekInYear(2017, 2017, 1, 1);
        assertFirstDayOfEpicWeekInYear(2016, 2016, 1, 3);
        assertFirstDayOfEpicWeekInYear(2015, 2015, 1, 4);
        assertFirstDayOfEpicWeekInYear(2014, 2013, 12, 29);
        assertFirstDayOfEpicWeekInYear(2013, 2012, 12, 30);
        assertFirstDayOfEpicWeekInYear(2012, 2012, 1, 1);
        assertFirstDayOfEpicWeekInYear(2011, 2011, 1, 2);
        assertFirstDayOfEpicWeekInYear(2010, 2010, 1, 3);
        assertFirstDayOfEpicWeekInYear(2009, 2009, 1, 4);
        assertFirstDayOfEpicWeekInYear(2008, 2007, 12, 30);
        assertFirstDayOfEpicWeekInYear(2007, 2006, 12, 31);
    }

    @Test
    public void weekOfYear() {
        assertWeekOfYear(2016, 1, 3,   /* ==>*/ 2016, 1);
        assertWeekOfYear(2015, 1, 1,   /* ==>*/ 2014, 52);
        assertWeekOfYear(2015, 1, 4,   /* ==>*/ 2015, 1);
        assertWeekOfYear(2015, 2, 2,   /* ==>*/ 2015, 5);
        assertWeekOfYear(2013, 1, 1,   /* ==>*/ 2013, 1);
        assertWeekOfYear(2013, 1, 30,  /* ==>*/ 2013, 5);
        assertWeekOfYear(2013, 3, 4,   /* ==>*/ 2013, 10);
        assertWeekOfYear(2013, 7, 31,  /* ==>*/ 2013, 31);
        assertWeekOfYear(2013, 8, 14,  /* ==>*/ 2013, 33);
        assertWeekOfYear(2013, 11, 14, /* ==>*/ 2013, 46);
        assertWeekOfYear(2013, 12, 11, /* ==>*/ 2013, 50);
        assertWeekOfYear(2013, 12, 28, /* ==>*/ 2013, 52);
        assertWeekOfYear(2013, 12, 29, /* ==>*/ 2014, 1);
        assertWeekOfYear(2013, 12, 30, /* ==>*/ 2014, 1);
        assertWeekOfYear(2013, 12, 31, /* ==>*/ 2014, 1);
        assertWeekOfYear(2012, 1, 1,   /* ==>*/ 2012, 1);
        assertWeekOfYear(2012, 1, 2,   /* ==>*/ 2012, 1);
        assertWeekOfYear(2011, 1, 1,   /* ==>*/ 2010, 52);
        assertWeekOfYear(2011, 2, 2,   /* ==>*/ 2011, 5);
        assertWeekOfYear(2011, 4, 27,  /* ==>*/ 2011, 17);
        assertWeekOfYear(2011, 12, 2,  /* ==>*/ 2011, 48);
        assertWeekOfYear(2011, 12, 29, /* ==>*/ 2011, 52);
        assertWeekOfYear(2011, 12, 31, /* ==>*/ 2011, 52);
    }

    private static void assertWeekOfYear(int year, int month, int dayOfMonth, int expectedYear, int expectedWeek) {
        assertThat(EpiWeek.weekOf(new LocalDate(year, month, dayOfMonth)),
                equalTo(new EpiWeek(expectedYear, expectedWeek)));
    }

    private void assertFirstDayOfEpicWeekInYear(int year, int expectedYear, int expectedMonth, int expectedDayOfMonth) {
        assertThat("Year " + year, EpiWeek.dayOfFirstEpiWeek(year),
                equalTo(new LocalDate(expectedYear, expectedMonth, expectedDayOfMonth)));
    }
}
