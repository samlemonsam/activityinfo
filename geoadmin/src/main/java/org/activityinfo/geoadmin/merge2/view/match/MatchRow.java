package org.activityinfo.geoadmin.merge2.view.match;


import com.google.common.base.Optional;
import org.activityinfo.geoadmin.merge2.model.InstanceMatch;
import org.activityinfo.model.resource.ResourceId;

public class MatchRow {

    public static final int UNMATCHED = -1;

    private boolean inputRequired = false;
    private boolean resolved = false;
    
    private final KeyFieldPairSet keyFields;
    private int sourceRow = UNMATCHED;
    private int targetRow = UNMATCHED;

    public MatchRow(KeyFieldPairSet keyFields) {
        this.keyFields = keyFields;
    }

    public void setSourceRow(int sourceRow) {
        this.sourceRow = sourceRow;
    }

    public void setTargetRow(int targetRow) {
        this.targetRow = targetRow;
    }


    public int getRow(MatchSide side) {
        if(side == MatchSide.SOURCE) {
            return getSourceRow();
        } else {
            return getTargetRow();
        }
    }
    
    public Optional<ResourceId> getSourceId() {
        if(sourceRow == UNMATCHED) {
            return Optional.absent();
        } 
        return Optional.of(keyFields.getSource().getRowId((sourceRow)));
    }
    
    public Optional<ResourceId> getTargetId() {
        if(targetRow == UNMATCHED) {
            return Optional.absent();
        }
        return Optional.of(keyFields.getTarget().getRowId(targetRow));
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

    public boolean isInputRequired() {
        return inputRequired;
    }

    public void setInputRequired(boolean inputRequired) {
        this.inputRequired = inputRequired;
    }

    public boolean isMatched() {
        return sourceRow != UNMATCHED && targetRow != UNMATCHED;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public InstanceMatch asInstanceMatch() {
        return new InstanceMatch(getSourceId().get(), getTargetId().get());
    }

    public boolean isMatched(MatchSide matchSide) {
        if(matchSide == MatchSide.SOURCE) {
            return sourceRow != UNMATCHED;
        } else {
            return targetRow != UNMATCHED;
        }
    }

}
