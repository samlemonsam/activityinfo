package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.type.time.FortnightValue;

import java.io.Serializable;
import java.util.function.IntFunction;

public class FortnightStringRenderer implements Serializable, IntFunction<String> {
    @Override
    public String apply(int value) {
        return FortnightValue.toString(DateEncoding.getYear(value), DateEncoding.getYearPart(value));
    }
}
