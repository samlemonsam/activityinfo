package org.activityinfo.geoadmin.merge.model;

public class ColumnPair {

    private MergeColumn target;
    private MergeColumn source;

    public ColumnPair(MergeColumn target, MergeColumn source) {
        this.target = target;
        this.source = source;
    }

    public MergeColumn getTarget() {
        return target;
    }

    public MergeColumn getSource() {
        return source;
    }
}

