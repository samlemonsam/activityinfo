package org.activityinfo.geoadmin.merge.model;


import com.google.common.base.Preconditions;

public class MatchRow {
    
    public static final int UNMATCHED = -1;
    
    private int target;
    private int source;

    private boolean deleted = false;

    public MatchRow(int target, int source) {
        this.target = target;
        this.source = source;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }
    
    public boolean isTargetMatched() {
        return target != UNMATCHED;
    }

    public boolean isSourceMatched() {
        return source != UNMATCHED;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    public boolean isMatched() {
        return isSourceMatched() && isTargetMatched();
    }

    public MatchRow split() {
        Preconditions.checkState(isMatched());
        
        MatchRow newRow = new MatchRow(UNMATCHED, source);
        newRow.deleted = deleted;
        source = UNMATCHED;
        
        return newRow;
    }

    /**
     * 
     * @return {@code true} if this {@code MatchRow} "fits" with an other MatchRow 
     */
    public boolean canMatch(MatchRow other) {
        return !isTargetMatched() && 
                isTargetMatched() == !other.isTargetMatched() &&
                isSourceMatched() == !other.isSourceMatched();
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
