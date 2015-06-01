package org.activityinfo.geoadmin.merge2.view.match;


import com.google.common.base.Optional;
import org.activityinfo.geoadmin.merge2.model.InstanceMatch;
import org.activityinfo.geoadmin.merge2.view.swing.merge.MatchLevel;
import org.activityinfo.model.resource.ResourceId;

public class MatchRow {

    public static final int UNMATCHED = -1;
    private final AutoRowMatching matching;

    private boolean resolved;
    private final FieldMatching mapping;
    private int sourceRow = UNMATCHED;
    private int targetRow = UNMATCHED;

    public MatchRow(AutoRowMatching rowMatching) {
        this.mapping = rowMatching.getFieldMatching();
        this.matching = rowMatching;
    }

    public void setSourceRow(int sourceRow) {
        this.sourceRow = sourceRow;
    }

    public void setTargetRow(int targetRow) {
        this.targetRow = targetRow;
    }

    public Optional<ResourceId> getSourceId() {
        if(sourceRow == UNMATCHED) {
            return Optional.absent();
        } 
        return Optional.of(mapping.getSource().getRowId((sourceRow)));
    }
    
    public Optional<ResourceId> getTargetId() {
        if(targetRow == UNMATCHED) {
            return Optional.absent();
        }
        return Optional.of(mapping.getTarget().getRowId(targetRow));
    }

    public int getSourceRow() {
        return sourceRow;
    }
    
    public int getTargetRow() {
        return targetRow;
    }

    public boolean isResolved() {
        return resolved;
    }

    /**
     * 
     * @return the lowest score among all the dimensions
     */
    public double getMinScore() {
        
        if(!isMatched()) {
            return Double.NaN;
        } else {
            double[] scores = matching.getScores(sourceRow, targetRow);
            if (scores == null || scores.length == 0) {
                return Double.NaN;
            } else {
                double minScore = scores[0];
                for (int i = 1; i < scores.length; ++i) {
                    if (scores[i] < minScore) {
                        minScore = scores[i];
                    }
                }
                return minScore;
            }
        }
    }
    
    public boolean isMatched() {
        return sourceRow != UNMATCHED && targetRow != UNMATCHED;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public MatchLevel getMatchLevel() {
        return MatchLevel.of(getMinScore());
    }
    
    public InstanceMatch asInstanceMatch() {
        return new InstanceMatch(getSourceId().get(), getTargetId().get());
    }
}
