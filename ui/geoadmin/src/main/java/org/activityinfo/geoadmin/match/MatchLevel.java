package org.activityinfo.geoadmin.match;


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
