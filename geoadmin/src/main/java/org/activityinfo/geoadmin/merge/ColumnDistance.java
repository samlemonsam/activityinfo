package org.activityinfo.geoadmin.merge;

import com.google.common.collect.Sets;
import org.activityinfo.geoadmin.match.DistanceFunction;

import java.util.Set;


/**
 * Computes the "distance" between two columns as a value between 0 and 1 where
 * at 0, 
 */
public class ColumnDistance implements DistanceFunction<MergeColumn> {

    @Override
    public int getDimensionCount() {
        return 1;
    }

    @Override
    public boolean compute(MergeColumn target, MergeColumn source, double[] result) {
        Set<String> targetValues = target.uniqueValues();
        Set<String> sourceValues = source.uniqueValues();
        
        double intersection = Sets.intersection(targetValues, sourceValues).size();
        if(intersection > 0) {
            double union = Sets.union(targetValues, sourceValues).size();
            result[0] = intersection / union;
            return true;

        } else {
            return false;
        }
    }
}
