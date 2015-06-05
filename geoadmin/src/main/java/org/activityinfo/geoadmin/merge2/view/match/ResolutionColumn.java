package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Optional;
import org.activityinfo.geoadmin.merge2.model.InstanceMatchSet;

/**
 * Displays the resolution status of the row
 */
public class ResolutionColumn extends MatchTableColumn {
    
    public static final String WARNING_ICON = "⚠";
    
    public static final String CHECK_ICON = "\u2713";
    
    
    private final MatchTable rows;
    private InstanceMatchSet matchSet;

    public ResolutionColumn(MatchTable model, InstanceMatchSet matchSet) {
        this.rows = model;
        this.matchSet = matchSet;
    }

    @Override
    public String getHeader() {
        return "";
    }

    @Override
    public String getValue(int rowIndex) {
        if(rows.isLoading()) {
            return null;
        }
        MatchRow row = rows.get(rowIndex);
        if(row.isInputRequired()) {
            if(row.isResolved()) {
                return CHECK_ICON;
            } else {
                return WARNING_ICON;
            }
        }
        return null;
    }

    @Override
    public Optional<MatchSide> getSide() {
        return Optional.absent();
    }
}
