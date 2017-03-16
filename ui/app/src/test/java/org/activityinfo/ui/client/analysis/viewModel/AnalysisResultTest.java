package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.junit.Test;

import java.util.Arrays;

public class AnalysisResultTest {

    @Test
    public void totalSets() {
        DimensionSet dimensionSet = new DimensionSet(Arrays.asList(
                new DimensionModel("G", "Gender").setTotalIncluded(true),
                new DimensionModel("A", "Age").setTotalIncluded(true),
                new DimensionModel("L", "Location").setTotalIncluded(true)));


        boolean totals[] = new boolean[dimensionSet.getCount()];
        while(AnalysisResult.nextTotalSet(dimensionSet, totals)) {
            System.out.println(Arrays.toString(totals));
        }
    }

}