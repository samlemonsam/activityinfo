package org.activityinfo.model.form;


public enum SubFormKind {
    REPEATING(false),
    MONTHLY(true),
    WEEKLY(true),
    BIWEEKLY(true),
    DAILY(true);

    private boolean period;

    SubFormKind(boolean period) {
        this.period = period;
    }

    public boolean isPeriod() {
        return period;
    }
}
