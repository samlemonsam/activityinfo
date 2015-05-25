package org.activityinfo.geoadmin.merge2.view.model;

/**
 * Created by alexander on 5/25/15.
 */
public class RowMatch {

    private final int targetRow;
    private final int sourceRow;

    public RowMatch(int targetRow, int sourceRow) {
        this.targetRow = targetRow;
        this.sourceRow = sourceRow;
    }

    public int getTargetRow() {
        return targetRow;
    }

    public int getSourceRow() {
        return sourceRow;
    }
}
