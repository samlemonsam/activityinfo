package org.activityinfo.geoadmin.merge2.view.match;

import org.activityinfo.geoadmin.match.ScoreMatrix;

import java.util.List;

/**
 * Matrix representing the distance between two sets of form instances.
 */
public class InstanceMatrix extends ScoreMatrix {
    
    private static final double MIN_SCORE = 0.5;

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
    public String[] getDimensionNames() {
        String[] names = new String[keyFields.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = keyFields.get(i).getTargetField().getLabel();
        }
        return names;
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
    public double score(int sourceIndex, int targetIndex, int dimensionIndex) {
        double score = keyFields.get(dimensionIndex).score(sourceIndex, targetIndex);
        if(score > MIN_SCORE) {
            return score;
        } else {
            // Discard low scores to avoid too much noise 
            return 0.0;
        }
    }
}
