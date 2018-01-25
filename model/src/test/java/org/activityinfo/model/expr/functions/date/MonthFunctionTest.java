package org.activityinfo.model.expr.functions.date;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class MonthFunctionTest {

    @Test
    public void fromIsoString() {
        assertThat(MonthFunction.fromIsoString("2014-05-11"), equalTo(5));
    }
}