package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.type.time.Month;

import java.io.Serializable;
import java.util.function.IntFunction;

public class MonthStringRenderer implements Serializable, IntFunction<String> {
    @Override
    public String apply(int value) {
        return Month.toString(DateEncoding.getYear(value), DateEncoding.getYearPart(value));
    }
}
