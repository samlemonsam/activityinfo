package org.activityinfo.model.type.time;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class LocalDateTest {

    
    @Test
    public void quarter() {
        assertThat(quarterOfMonth(1),  equalTo(1));
        assertThat(quarterOfMonth(2),  equalTo(1));
        assertThat(quarterOfMonth(3),  equalTo(1));
        
        assertThat(quarterOfMonth(4),  equalTo(2));
        assertThat(quarterOfMonth(5),  equalTo(2));
        assertThat(quarterOfMonth(6),  equalTo(2));
        
        assertThat(quarterOfMonth(7),  equalTo(3));
        assertThat(quarterOfMonth(8),  equalTo(3));
        assertThat(quarterOfMonth(9),  equalTo(3));
        
        assertThat(quarterOfMonth(10), equalTo(4));
        assertThat(quarterOfMonth(11), equalTo(4));
        assertThat(quarterOfMonth(12), equalTo(4));

    }

    @Test
    public void dayOfYear() {
        // non leap year
        assertThat(new LocalDate(2017, 1, 1).getDayOfYear(), equalTo(1));
        assertThat(new LocalDate(2017, 2, 1).getDayOfYear(), equalTo(32));
        assertThat(new LocalDate(2017, 2, 10).getDayOfYear(), equalTo(41));
        assertThat(new LocalDate(2017, 3, 4).getDayOfYear(), equalTo(63));
        assertThat(new LocalDate(2017, 7, 13).getDayOfYear(), equalTo(194));
        assertThat(new LocalDate(2017, 12, 31).getDayOfYear(), equalTo(365));

        // leap year
        assertThat(new LocalDate(2004, 1, 1).getDayOfYear(), equalTo(1));
        assertThat(new LocalDate(2004, 2, 1).getDayOfYear(), equalTo(32));
        assertThat(new LocalDate(2004, 2, 10).getDayOfYear(), equalTo(41));
        assertThat(new LocalDate(2004, 3, 4).getDayOfYear(), equalTo(64));
        assertThat(new LocalDate(2004, 7, 13).getDayOfYear(), equalTo(195));
        assertThat(new LocalDate(2004, 12, 31).getDayOfYear(), equalTo(366));
    }


    private int quarterOfMonth(int month) {
        LocalDate date = new LocalDate(2015, month, 1);
        return date.getQuarter();
    }
}