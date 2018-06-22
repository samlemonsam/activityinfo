package org.activityinfo.store.hrd.columns;

import java.io.Serializable;
import java.util.function.IntFunction;

public class MonthStringRenderer implements Serializable, IntFunction<String> {
    @Override
    public String apply(int value) {
        return DateEncoding.getYearPart(value) + "-" + DateEncoding.getYearPart(value);
    }
}
