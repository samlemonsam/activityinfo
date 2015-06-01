package org.activityinfo.geoadmin.merge2.view.match;


public class AutoRowMatching {
    private final FieldMatching fieldMatching;
    private RowDistanceMatrix matrix;
    private final int[] assignment;

    public AutoRowMatching(FieldMatching fieldMatching, RowDistanceMatrix matrix, int[] assignment) {
        this.fieldMatching = fieldMatching;
        this.matrix = matrix;
        this.assignment = assignment;
    }

    public FieldMatching getFieldMatching() {
        return fieldMatching;
    }

    public int getBestSourceMatchForTarget(int targetRowIndex) {
        return assignment[targetRowIndex];
    }

    public double[] getScores(int sourceRow, int targetRow) {
        return matrix.getScores(targetRow, sourceRow);
    }
}
