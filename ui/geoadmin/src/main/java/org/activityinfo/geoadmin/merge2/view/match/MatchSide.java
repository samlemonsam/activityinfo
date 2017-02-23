package org.activityinfo.geoadmin.merge2.view.match;


public enum MatchSide {
    TARGET {
        @Override
        public MatchSide opposite() {
            return SOURCE;
        }
    },
    SOURCE {
        @Override
        public MatchSide opposite() {
            return TARGET;
        }
    };
    
    public abstract MatchSide opposite();
}
