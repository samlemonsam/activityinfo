package org.activityinfo.geoadmin.merge2.view.swing.match;

import org.activityinfo.geoadmin.merge2.view.match.MatchSide;

/**
 * Current selection within the match table
 */
public class MatchTableSelection {
    private int matchIndex;
    private MatchSide matchSide;

    public int getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(int matchIndex) {
        this.matchIndex = matchIndex;
    }

    public MatchSide getMatchSide() {
        return matchSide;
    }

    public void setMatchSide(MatchSide matchSide) {
        this.matchSide = matchSide;
    }
}
