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

    private int quarterOfMonth(int month) {
        LocalDate date = new LocalDate(2015, month, 1);
        return date.getQuarter();
    }
}