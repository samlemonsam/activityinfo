package org.activityinfo.model.formula.functions.date;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class QuarterFunctionTest {

    @Test
    public void test() {
        assertThat(QuarterFunction.fromMonth(1), equalTo(1));
        assertThat(QuarterFunction.fromMonth(2), equalTo(1));
        assertThat(QuarterFunction.fromMonth(3), equalTo(1));
        assertThat(QuarterFunction.fromMonth(4), equalTo(2));
        assertThat(QuarterFunction.fromMonth(5), equalTo(2));
        assertThat(QuarterFunction.fromMonth(6), equalTo(2));
        assertThat(QuarterFunction.fromMonth(7), equalTo(3));
        assertThat(QuarterFunction.fromMonth(8), equalTo(3));
        assertThat(QuarterFunction.fromMonth(9), equalTo(3));
        assertThat(QuarterFunction.fromMonth(10), equalTo(4));
        assertThat(QuarterFunction.fromMonth(11), equalTo(4));
        assertThat(QuarterFunction.fromMonth(12), equalTo(4));
    }
}