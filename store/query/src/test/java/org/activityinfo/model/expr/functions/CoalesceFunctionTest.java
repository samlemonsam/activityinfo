package org.activityinfo.model.expr.functions;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.junit.Test;

import static java.lang.Double.NaN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class CoalesceFunctionTest {

    @Test
    public void combineDouble() {
        DoubleArrayColumnView x = new DoubleArrayColumnView(new double[] { 80, NaN, 82, NaN,  84});
        DoubleArrayColumnView y = new DoubleArrayColumnView(new double[] { 90, NaN, 92,  93, NaN });

        ColumnView z = CoalesceFunction.combineDouble(new ColumnView[]{x, y});
        
        assertThat(z.getDouble(0), equalTo(80.0));
        assertThat(z.getDouble(1), equalTo(NaN));
        assertThat(z.getDouble(2), equalTo(82.0));
        assertThat(z.getDouble(3), equalTo(93.0));
        assertThat(z.getDouble(4), equalTo(84.0));
    }
}