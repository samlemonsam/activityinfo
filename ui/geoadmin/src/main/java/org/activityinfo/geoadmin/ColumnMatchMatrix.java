/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.geoadmin;

/**
 * Matches a set of n columns to the most similar m columns
 */
public class ColumnMatchMatrix {

    public static final double MIN_SCORE = 0d;
    private double scores[][];
    private int counts[][];
    private int rows;
    private int cols;

    public ColumnMatchMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        scores = new double[rows][cols];
        counts = new int[rows][cols];
    }

    public void addScore(int row, int col, double score) {
        scores[row][col] += score;
        counts[row][col] ++;
    }

    /**
     * Optimal mapping from left to right columns
     */
    public int[] solve() {

        normalizeScores();

        // keep track of matched/unmatched columns
        int matchedRows[] = new int[rows];
        boolean matchedCols[] = new boolean[cols];


        // first, find the best, dominant match for each column in the left
        for(int i=0;i!=rows;++i) {
            int bestCol = findColMax(i, matchedCols);
            if(bestCol >= 0 && findRowMax(bestCol) == i) {
                matchedRows[i] = bestCol;
                matchedCols[bestCol] = true;
            } else {
                matchedRows[i] = -1;
            }
        }

        // now, find the best among what's left over
        // (pretty arbitrary order)
        for(int i=0;i!=rows;++i) {
            int bestCol = findColMax(i, matchedCols);
            if(bestCol >= 0) {
                matchedRows[i] = bestCol;
                matchedCols[bestCol] = true;
            }
        }

        return matchedRows;
    }

    private void normalizeScores() {
        for(int i=0;i!=rows;++i) {
            for (int j = 0; j < cols; ++j) {
                double count = counts[i][j];
                if(count > 0) {
                    scores[i][j] /= count;
                }
            }
        }
    }

    private int findColMax(int i, boolean[] matchedCols) {
        int maxCol = -1;
        double max = MIN_SCORE;
        for(int j=0;j<cols;++j) {
            if(!matchedCols[j]) {
                if (scores[i][j] > max) {
                    max = scores[i][j];
                    maxCol = j;
                }
            }
        }
        return maxCol;
    }

    private int findRowMax(int j) {
        int maxRow = -1;
        double max = MIN_SCORE;
        for(int i=0;i<rows;++i) {
            if(scores[i][j] > max) {
                max = scores[i][j];
                maxRow = i;
            }
        }
        return maxRow;
    }

}
