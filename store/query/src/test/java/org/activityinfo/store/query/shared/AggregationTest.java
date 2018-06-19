package org.activityinfo.store.query.shared;

import org.activityinfo.model.formula.functions.CountFunction;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AggregationTest {

    @Test
    public void emptyArray() {

        int[] groupId = new int[0];
        double[] values = new double[0];
        int numGroups = 21;

        double[] result = Aggregation.sortAndAggregate(CountFunction.INSTANCE, groupId, values, 0, numGroups);

        assertThat(result.length, equalTo(numGroups));
    }

}