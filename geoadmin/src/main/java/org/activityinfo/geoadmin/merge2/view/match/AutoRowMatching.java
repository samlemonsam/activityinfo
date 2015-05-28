package org.activityinfo.geoadmin.merge2.view.match;


public class AutoRowMatching {
    private final FormMapping formMapping;
    private RowDistanceMatrix matrix;
    private final int[] assignment;

    public AutoRowMatching(FormMapping formMapping, RowDistanceMatrix matrix, int[] assignment) {
        this.formMapping = formMapping;
        this.matrix = matrix;
        this.assignment = assignment;
    }

    public FormMapping getFormMapping() {
        return formMapping;
    }

    public int getBestSourceMatchForTarget(int targetRowIndex) {
        return assignment[targetRowIndex];
    }

    public double[] getScores(int sourceRow, int targetRow) {
        return matrix.getScores(targetRow, sourceRow);
    }
}
