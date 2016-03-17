package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Optional;
import org.activityinfo.geoadmin.match.MatchLevel;

/**
 * Single column in the merge table
 */
public abstract class MatchTableColumn {

    public abstract String getHeader();

    public abstract String getValue(int rowIndex);

    public abstract Optional<MatchSide> getSide();
    
    public Optional<MatchLevel> getMatchConfidence(int rowIndex) {
        return Optional.absent();
    }

}
