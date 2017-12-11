package org.activityinfo.model.query;

import java.util.List;

public class SortModel {

    private String field;
    private Dir dir = Dir.NONE;

    /**
     * {@code Dir} gives the direction of ordering:
     *  {@code ASC} (Ascending);
     *  {@code DSC} (Descending); or
     *  {@code NONE} (Undefined)
     */
    public enum Dir {
        ASC,
        DESC,
        NONE
    }

    /**
     * The {@code Range} stores the indices of a group's member elements.
     * Range object allows for efficient re-selection of various row ranges,
     * limiting the creation of new int array instances
     */
    public static class Range {

        // max array length set during initial construction
        private int[] rows;
        private int endIndex;

        public Range(int from, int to) {
            this.rows = new int[to-from+1];
            for (int i=0; i<rows.length; i++) {
                rows[i] = from + i;
            }
            this.endIndex = rows.length-1;
        }

        public Range(int[] range) {
            this.rows = range;
            this.endIndex = rows.length-1;
        }

        public Range(List<Integer> range) {
            this.rows = new int[range.size()];
            for (int i=0; i<range.size(); i++) {
                rows[i] = range.get(i);
            }
            this.endIndex = rows.length-1;
        }

        public int getRangeSize() {
            return endIndex+1;
        }

        /**
         * Returns the currently selected row range.
         * Only creates a new int array when a subset of the full range is selected.
         */
        public int[] getRange() {
            return endIndex == (rows.length-1) ? rows : copySubset(rows,0, endIndex);
        }

        public Range resetRange() {
            endIndex = -1;
            return this;
        }

        public void addToRange(int row) {
            rows[endIndex+1] = row;
            endIndex++;
        }

        private int[] copySubset(int[] array, int from, int to) {
            int[] subset = new int[to-from+1];
            for (int i=0; i<subset.length; i++) {
                subset[i] = array[from + i];
            }
            return subset;
        }
    }

    public SortModel(String field, Dir dir) {
        this.field = field;
        this.dir = dir;
    }

    public String getField() {
        return field;
    }

    public Dir getDir() {
        return dir;
    }

}
