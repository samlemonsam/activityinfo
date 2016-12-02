package org.activityinfo.model.date;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by yuriyz on 8/12/2016.
 */
public class EpiWeekTest {

    @Test
    public void serialization() {
        EpiWeek epiWeek = new EpiWeek(3, 2016);
        EpiWeek deserialized = EpiWeek.parse(epiWeek.toString());

        assertEquals(epiWeek, deserialized);
    }

    @Test
    public void plus() {
        EpiWeek epiWeek = new EpiWeek(3, 2016);

        assertEquals(epiWeek.plus(1), new EpiWeek(4, 2016));
        assertEquals(epiWeek.plus(10), new EpiWeek(13, 2016));
        assertEquals(epiWeek.plus(52), new EpiWeek(3, 2017));
    }

    @Test
    public void dateRange() {
        assertDateRange(2013, 1, new LocalDate(2012, 12, 30), new LocalDate(2013, 1, 5));
        assertDateRange(2013, 9, new LocalDate(2013, 2, 24), new LocalDate(2013, 3, 2));
        assertDateRange(2013, 27, new LocalDate(2013, 6, 30), new LocalDate(2013, 7, 6));
        assertDateRange(2013, 52, new LocalDate(2013, 12, 22), new LocalDate(2013, 12, 28));
    }

    private static void assertDateRange(int year, int weekInYear, LocalDate startDate, LocalDate endDate) {
        assertEquals(new EpiWeek(weekInYear, year).getDateRange(), new DateRange(startDate.atMidnightInMyTimezone(), endDate.atMidnightInMyTimezone()));
    }
}
