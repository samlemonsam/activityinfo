package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Optional;

/**
 * Provides visual separation between the target and source columns
 */
public class SeparatorColumn extends MatchTableColumn {
    @Override
    public String getHeader() {
        return "";
    }

    @Override
    public String getValue(int rowIndex) {
        return null;
    }

    @Override
    public Optional<MatchSide> getSide() {
        return Optional.absent();
    }
}
