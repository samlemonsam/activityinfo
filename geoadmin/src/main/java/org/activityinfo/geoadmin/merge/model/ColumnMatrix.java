package org.activityinfo.geoadmin.merge.model;

import com.google.common.collect.Sets;
import org.activityinfo.geoadmin.match.DistanceMatrix;

import java.util.List;
import java.util.Set;


/**
 * Computes the "distance" between two columns as a value between 0 and 1 where
 * at 0, 
 */
public class ColumnMatrix extends DistanceMatrix {

    private List<MergeColumn> targetColumns;
    private List<MergeColumn> sourceColumns;
    private double[][] distance;

    public ColumnMatrix(List<MergeColumn> targetColumns, List<MergeColumn> sourceColumns) {
        this.targetColumns = targetColumns;
        this.sourceColumns = sourceColumns;
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

        double intersection = Sets.intersection(targetValues, sourceValues).size();
        if(intersection > 0) {
            double union = Sets.union(targetValues, sourceValues).size();
            return intersection / union;

        } else {
            return 0d;
        }
    }
}
