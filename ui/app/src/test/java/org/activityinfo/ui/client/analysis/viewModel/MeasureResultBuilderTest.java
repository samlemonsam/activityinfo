package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.ui.client.analysis.model.ImmutableDimensionModel;
import org.junit.Test;

import java.util.Arrays;

public class MeasureResultBuilderTest {

    @Test
    public void totalSets() {
        DimensionSet dimensionSet = new DimensionSet(Arrays.asList(
                ImmutableDimensionModel.builder().id("G").label("Gender").totalIncluded(true).build(),
                ImmutableDimensionModel.builder().id("A").label("Age").totalIncluded(true).build(),
                ImmutableDimensionModel.builder().id("L").label("Location").totalIncluded(true).build()));

        boolean totals[] = new boolean[dimensionSet.getCount()];
        while(MeasureResultBuilder.nextSubset(dimensionSet, totals)) {
            System.out.println(Arrays.toString(totals));
        }
    }

}