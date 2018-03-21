package org.activityinfo.model.type.time;

import org.junit.Test;

import static org.activityinfo.model.type.time.LocalDateInterval.month;
import static org.activityinfo.model.type.time.LocalDateInterval.year;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalDateIntervalTest {

    @Test
    public void overlaps() {
        assertFalse(year(2017).overlaps(year(2018)));
        assertTrue(month(2017, 1).overlaps(year(2017)));
        assertFalse(month(2017, 1).overlaps(month(2017, 2)));
    }

}