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
package org.activityinfo.ui.client.component.importDialog.model.source;

import java.util.Iterator;

/**
 * Guesses the delimiter used in an a text file
 */
public class DelimiterGuesser {

    private static final char[] POSSIBLE_DELIMITERS = new char[]{',', ';', '\t', '|'};
    private static final int ROWS_TO_SCAN = 10;

    private final String text;
    private int firstNotMatchedRow = -1;
    private boolean isDataSetOfOneColumn = false;

    public DelimiterGuesser(String text) {
        this.text = text;
    }

    public char guess() {
        // first, look for a delimiter that divides the columns into
        // a consistent number of columns > 1
        for (char delimiter : POSSIBLE_DELIMITERS) {
            if (matchColumnCount(delimiter, true)) {
                return delimiter;
            }
        }

        // If that doesn't work, try delimiters that divides the rows into
        // columns that are less than or equal to the number of headers.
        // (Sometimes empty tabs get trimmed)
        if(matchColumnCount('\t', false)) {
            return '\t';
        }

        // if not, then assume that this is a dataset of 1 column
        isDataSetOfOneColumn = true;
        return '\0';
    }

    /**
     * Checks to see whether the first {@code ROWS_TO_SCAN} have the same column count
     * when using the given {@code delimiter}.
     * @param delimiter the delimiter to try
     * @param strict if true, the column count must be exactly equal. if false, the column count in subsequent rows
     *               must be less than or equal to the header column count
     * @return true if the column counts match using this delimiter
     */
    private boolean matchColumnCount(char delimiter, boolean strict) {

        // we expect a delimiter to divide the input data set into
        // a more or less similar number of columns

        Iterator<PastedRow> it = new RowParser(text, delimiter)
                .parseRows(ROWS_TO_SCAN).iterator();

        int numHeaders = it.next().getColumnCount();
        if (numHeaders == 1) {
            return false;
        }

        int rowCount = 0;
        int matchedRowCount = 0;
        while(it.hasNext()) {
            int columnCount = it.next().getColumnCount();
            if(  (strict && columnCount == numHeaders) ||
                (!strict && columnCount <= numHeaders)) {
                matchedRowCount++;

            } else {
                if (firstNotMatchedRow < 0) {
                    firstNotMatchedRow = rowCount+1;
                }
            }
            rowCount++;
        }

        return matchedRowCount == rowCount;
    }

    public int getFirstNotMatchedRow() {
        return firstNotMatchedRow;
    }

    public boolean isDataSetOfOneColumn() {
        return isDataSetOfOneColumn;
    }
}
