package org.activityinfo.geoadmin.merge2.view.model;



public class MatchRow {

    public static final int UNMATCHED = -1;
    
    private final int sourceRow;
    private final int targetRow;
    private final double scores[];

    public MatchRow(int sourceRow, int targetRow, double scores[]) {
        this.targetRow = targetRow;
        this.sourceRow = sourceRow;
        this.scores = scores;
    }

    public int getSourceRow() {
        return sourceRow;
    }
    
    public int getTargetRow() {
        return targetRow;
    }

    public double[] getScores() {
        return scores;
    }

    /**
     * 
     * @return the lowest score among all the dimensions
     */
    public double getMinScore() {
        if(scores == null) {
            return Double.NaN;
        } else {
            double minScore = scores[0];
            for(int i=1;i<scores.length;++i) {
                if(scores[i] < minScore) {
                    minScore = scores[i];
                }
            }
            return minScore;
        }
    }
    
    public boolean isMatched() {
        return sourceRow != UNMATCHED && targetRow != UNMATCHED;
    }
}
