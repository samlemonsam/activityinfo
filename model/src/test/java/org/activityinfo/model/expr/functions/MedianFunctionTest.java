package org.activityinfo.model.expr.functions;

import org.junit.Test;

import static java.lang.Double.NaN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MedianFunctionTest {

    @Test
    public void emptyValues() {
        assertTrue(Double.isNaN(median()));
    }

    @Test
    public void singleValue() {
        assertThat(median(41), equalTo(41.0));
    }

    @Test
    public void evenNumberOfValues() {
        assertThat(median(3,1), equalTo(2.0));
        assertThat(median(3,1,6,4), equalTo(3.5));
    }

    @Test
    public void oddNumberOfValues() {
        assertThat(median(3, 9, 1), equalTo(3.0));
        assertThat(median(3, 9, 1, 5, 6), equalTo(5.0));
    }

    @Test
    public void missingValues() {
        assertThat(median(NaN, 4, NaN, 1, NaN, 6, NaN, NaN), equalTo(4.0));
    }

    @Test
    public void partOfArray() {
        double values[] = new double[] { 1, 4, 5, 6, 7, 9 };

        assertThat(MedianFunction.INSTANCE.compute(values, 0, 3), equalTo(4.0));
        assertThat(MedianFunction.INSTANCE.compute(values, 1, 3), equalTo(4.5));
    }

    private double median(double... values) {
        return MedianFunction.INSTANCE.compute(values, 0, values.length);
    }

}