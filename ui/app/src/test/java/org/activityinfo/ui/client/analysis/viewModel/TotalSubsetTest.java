/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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