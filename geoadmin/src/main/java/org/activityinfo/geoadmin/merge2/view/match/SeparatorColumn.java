package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Optional;

/**
 * Provides visual separation between the target and source columns
 */
public class SeparatorColumn extends MatchTableColumn {
    
    private final MatchTable matchTable;

    public SeparatorColumn(MatchTable matchTable) {
        this.matchTable = matchTable;
    }

    @Override
    public String getHeader() {
        return "";
    }

    @Override
    public String getValue(int rowIndex) {
        MatchRow matchRow = matchTable.get(rowIndex);
        if(matchRow.getSourceRow() != MatchRow.UNMATCHED) {
            return "\u00BB";
        }
        return null;
    }

    @Override
    public Optional<MatchSide> getSide() {
        return Optional.absent();
    }
}
