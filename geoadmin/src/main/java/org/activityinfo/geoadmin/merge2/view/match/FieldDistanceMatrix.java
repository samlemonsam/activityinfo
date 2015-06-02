package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.collect.Sets;
import org.activityinfo.geoadmin.match.DistanceMatrix;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;

import java.util.List;
import java.util.Set;

/**
 * Computes the "distance" between the <em>contents</em> of two fields
 *
 */
public class FieldDistanceMatrix extends DistanceMatrix {
    private List<FieldProfile> sourceColumns;
    private List<FieldProfile> targetColumns;

    public FieldDistanceMatrix(List<FieldProfile> sourceColumns, List<FieldProfile> targetColumns) {
        this.sourceColumns = sourceColumns;
        this.targetColumns = targetColumns;
    }

    @Override
    public int getDimensionCount() {
        return 1;
    }


    @Override
    public int getRowCount() {
        return targetColumns.size();
    }

    @Override
    public int getColumnCount() {
        return sourceColumns.size();
    }

    @Override
    public boolean matches(int rowIndex, int columnIndex) {
        return sumScores(rowIndex, columnIndex) > 0.25;
    }

    @Override
    public double score(int i, int j, int d) {
        Set<String> targetValues = targetColumns.get(i).uniqueValues();
        Set<String> sourceValues = sourceColumns.get(j).uniqueValues();
        
        // Alternative: Relative information
        // I(s; p) = sum(i = 1 to n)  s[i] * log(s[i]/p[i])

        double intersection = Sets.intersection(targetValues, sourceValues).size();
        if (intersection > 0) {
            double union = Sets.union(targetValues, sourceValues).size();
            return intersection / union;

        } else {
            return 0d;
        }
    }
}
