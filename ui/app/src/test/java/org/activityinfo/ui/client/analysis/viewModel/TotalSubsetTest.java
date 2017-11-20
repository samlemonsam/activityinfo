package org.activityinfo.ui.client.analysis.viewModel;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class TotalSubsetTest {

    @Test
    public void totalSets() {
        boolean totals[] = new boolean[3];
        boolean totalsRequired[] = new boolean[] { true, true, true };


        List<boolean[]> subsets = new ArrayList<>();
        while(TotalSubset.nextSubset(totals, totalsRequired)) {
            System.out.println(Arrays.toString(totals));
            subsets.add(Arrays.copyOf(totals, totals.length));
        }

        assertThat(subsets, hasSize(7));
        assertThat(Arrays.toString(subsets.get(0)), equalTo("[false, false, true]"));
        assertThat(Arrays.toString(subsets.get(1)), equalTo("[false, true, false]"));
        assertThat(Arrays.toString(subsets.get(2)), equalTo("[false, true, true]"));
        assertThat(Arrays.toString(subsets.get(3)), equalTo("[true, false, false]"));
        assertThat(Arrays.toString(subsets.get(4)), equalTo("[true, false, true]"));
        assertThat(Arrays.toString(subsets.get(5)), equalTo("[true, true, false]"));
        assertThat(Arrays.toString(subsets.get(6)), equalTo("[true, true, true]"));
    }

}