package org.activityinfo.model.date;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by yuriyz on 8/15/2016.
 */
public class BiWeekTest {

    @Test
    public void serialization() {
        BiWeek week = new BiWeek(2, 2016);
        BiWeek deserialized = BiWeek.parse(week.toString());

        assertEquals(week, deserialized);
    }

    @Test
    public void plus() {
        BiWeek week = new BiWeek(2, 2016);

        assertEquals(week.plus(1), new BiWeek(4, 2016));
        assertEquals(week.plus(10), new BiWeek(22, 2016));
    }
}
