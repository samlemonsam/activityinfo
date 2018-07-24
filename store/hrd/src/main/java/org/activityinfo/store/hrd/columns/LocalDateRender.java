package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.type.time.LocalDate;

import java.io.Serializable;
import java.util.function.IntFunction;

public class LocalDateRender implements Serializable, IntFunction<String> {
    @Override
    public String apply(int value) {
        return LocalDate.toString(
                DateEncoding.getYear(value),
                DateEncoding.getLocalDateMonth(value),
                DateEncoding.getLocalDateDay(value));
    }
}
