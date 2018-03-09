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
package org.activityinfo.store.query.shared;

import org.activityinfo.model.formula.functions.StatFunction;

import java.util.Arrays;

/**
 * Applies an aggregation function to a grouped list of values.
 */
public final class Aggregation {

    private Aggregation() {}

    /**
     * Sorts the {@code group} and {@code values} array in tandem and calculates the statistic per group.
     *
     * <p>Note: the {@code group} and {@code values} array are sorted in place!</p>
     *
     * @param statistic the statistic to apply
     * @param groupId an array of at least length {@code numValues} containing group ids
     * @param values an array of at least length {@code numValues} containing the values to be aggregated
     * @param numValues number of values to aggregate.
     * @param numGroups number of groups present. The maximum value in the groupId values should be {@code numGroups-1}
     * @return an array of length {@code numGroups} containing the aggregated values.
     */
    public static double[] sortAndAggregate(StatFunction statistic, int[] groupId, double[] values,
                                            int numValues, int numGroups) {


        if(numValues == 0) {
            return new double[] { statistic.compute(values, 0, 0) };
        }

        HeapsortTandem.heapsortDescending(groupId, values, numValues);

        return aggregateSorted(statistic, groupId, values, numValues, numGroups);


    }


    /**
     * Aggregate a value list already sorted by group.
     *
     * @param statistic
     * @param groupId
     * @param values
     * @param numValues
     * @param numGroups
     * @return
     */
    public static double[] aggregateSorted(StatFunction statistic, int[] groupId, double[] values, int numValues, int numGroups) {
        // Allocate the output for the results
        double result[] = new double[numGroups];
        Arrays.fill(result, Double.NaN);

        // Start at the first group
        int groupStart = 0;

        do {
            int masterRow = groupId[groupStart];

            // Find where this group ends
            int groupEnd = groupStart + 1;
            while (groupEnd < numValues && masterRow == groupId[groupEnd]) {
                groupEnd++;
            }

            // Compute the statistic over this group
            if(masterRow != -1) {
                result[masterRow] = statistic.compute(values, groupStart, groupEnd);
            }
            // Move to the next group
            groupStart = groupEnd;

        } while(groupStart < numValues);
        return result;
    }
}
