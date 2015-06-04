package org.activityinfo.geoadmin.merge2.view.match;

import org.activityinfo.geoadmin.match.ScoreMatrix;

import java.util.List;

/**
 * Matrix representing the distance between two sets of form instances.
 */
public class InstanceMatrix extends ScoreMatrix {

    private int sourceCount;
    private int targetCount;
    private int dimensionCount;

    private List<KeyFieldPair> keyFields;

    public InstanceMatrix(KeyFieldPairSet keyFields) {
        dimensionCount = keyFields.size();
        this.sourceCount = keyFields.getSourceCount();
        this.targetCount = keyFields.getTargetCount();
        this.keyFields = keyFields.asList();
    }

    @Override
    public int getDimensionCount() {
        return dimensionCount;
    }

    @Override
    public int getRowCount() {
        return sourceCount;
    }

    @Override
    public int getColumnCount() {
        return targetCount;
    }

    @Override
    public boolean matches(int sourceIndex, int targetIndex) {
        for(int d=0;d<dimensionCount;++d) {
            double score = keyFields.get(d).score(sourceIndex, targetIndex);
            if(score > .50) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double score(int sourceIndex, int targetIndex, int dimensionIndex) {
       return keyFields.get(dimensionIndex).score(sourceIndex, targetIndex);
    }
}
