package org.activityinfo.geoadmin.merge.model;

import com.google.common.collect.BiMap;
import org.activityinfo.geoadmin.match.DistanceMatrix;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;
import org.activityinfo.model.query.ColumnView;

import java.util.Map;


public class RowDistanceMatrix extends DistanceMatrix {
    
    private ColumnView target[];
    private ColumnView source[];
    
    private int targetRowCount;
    private int sourceRowCount;
    
    private int dimensionCount;
    private LatinPlaceNameScorer placeNameScorer = new LatinPlaceNameScorer();

    public RowDistanceMatrix(MergeForm targetForm, MergeForm sourceForm, BiMap<MergeColumn, MergeColumn> columnMapping) {
        dimensionCount = columnMapping.size();
        this.target = new ColumnView[dimensionCount];
        this.source = new ColumnView[dimensionCount];
        this.targetRowCount = targetForm.getRowCount();
        this.sourceRowCount = sourceForm.getRowCount();
        
        int columnIndex = 0;
        for (Map.Entry<MergeColumn, MergeColumn> mapping : columnMapping.entrySet()) {
            target[columnIndex] = mapping.getKey().getView();    
            source[columnIndex] = mapping.getValue().getView();
            columnIndex++;
        }    
    }
    
    @Override
    public int getDimensionCount() {
        return dimensionCount;
    }

    @Override
    public int getRowCount() {
        return targetRowCount;
    }

    @Override
    public int getColumnCount() {
        return sourceRowCount;
    }

    @Override
    public boolean matches(int targetRowIndex, int sourceRowIndex) {
        LatinPlaceNameScorer placeNameScorer = new LatinPlaceNameScorer();
        for(int d=0;d<dimensionCount;++d) {
            if(score(targetRowIndex, sourceRowIndex, d) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double score(int targetRowIndex, int sourceRowIndex, int dimensionIndex) {
        String targetValue = target[targetRowIndex].getString(targetRowIndex);
        String sourceValue = source[sourceRowIndex].getString(sourceRowIndex);
        return placeNameScorer.score(targetValue, sourceValue);
    }
}
