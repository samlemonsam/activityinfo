package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.type.time.EpiWeek;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.Month;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class DateEncodingTest {

    @Test
    public void monthEncoding() {
        for (int month = 1; month <= 12; month++) {
            checkRoundTrip(new Month(2018, month));
        }
    }

    @Test
    public void dateEncoding() {
        LocalDate start = new LocalDate(1999, 11, 15);
        for (int i = 0; i < 400; i++) {
            checkRoundTrip(start.plusDays(i));
        }
    }

    @Test
    public void ordering() {
        int jan2012 = DateEncoding.encodeMonth(new Month(2012, 1));
        int dec2012 = DateEncoding.encodeMonth(new Month(2012, 12));
        int mar2010 = DateEncoding.encodeMonth(new Month(2010, 3));

        assertThat(jan2012, lessThan(dec2012));
        assertThat(jan2012, greaterThan(mar2010));
    }

    @Test
    public void formatting() {
        MonthStringRenderer renderer = new MonthStringRenderer();
        int jan2012 = DateEncoding.encodeMonth(new Month(2012, 1));

        assertThat(renderer.apply(jan2012), equalTo("2012-01"));
    }

    @Test
    public void epiWeeks() {
        int week = DateEncoding.encodeWeek(new EpiWeek(2014, 50));
        assertThat(DateEncoding.getYear(week), equalTo(2014));
        assertThat(DateEncoding.getYearPart(week), equalTo(50));

        WeekColumnRenderer renderer = new WeekColumnRenderer();
        assertThat(renderer.apply(week), equalTo(new EpiWeek(2014, 50).toString()));

    }

    private void checkRoundTrip(Month month) {
        int encoded = DateEncoding.encodeMonth(month);
        Month decoded = DateEncoding.decodeMonth(encoded);

        assertThat(decoded, equalTo(month));
    }

    private void checkRoundTrip(LocalDate date) {
        int encoded = DateEncoding.encodeLocalDate(date);
        LocalDate decoded = DateEncoding.decodeLocalDate(encoded);

        assertThat(decoded, equalTo(date));
    }
}