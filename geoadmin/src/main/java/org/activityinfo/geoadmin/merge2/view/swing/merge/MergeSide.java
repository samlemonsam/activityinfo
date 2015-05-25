package org.activityinfo.geoadmin.merge2.view.swing.merge;


public enum MergeSide {
    TARGET {
        @Override
        public MergeSide opposite() {
            return SOURCE;
        }
    },
    SOURCE {
        @Override
        public MergeSide opposite() {
            return TARGET;
        }
    };
    
    public abstract MergeSide opposite();
}
