package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.type.time.EpiWeek;

import java.io.Serializable;
import java.util.function.IntFunction;

public class WeekColumnRenderer implements Serializable, IntFunction<String> {
    @Override
    public String apply(int value) {
        return EpiWeek.toString(DateEncoding.getYear(value), DateEncoding.getYear(value));
    }
}
