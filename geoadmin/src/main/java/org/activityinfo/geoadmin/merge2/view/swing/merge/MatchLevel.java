package org.activityinfo.geoadmin.merge2.view.swing.merge;


public enum MatchLevel {
    
    EXACT,
    WARNING,
    POOR;
    
    public static MatchLevel of(double value) {
        if(value > 0.98) {
            return EXACT;
        } else if(value > 0.90) {
            return WARNING;
        } else {
            return POOR;
        }
    }
    
}
