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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Parses delimited text files into rows and columns
 */
public class RowParser {

    public static final char QUOTE_CHAR = '"';

    private String text;
    private int length;
    private int currentPos = 0;
    private char delimiter;
    private int rowIndex;
    private int maxRowCount = Integer.MAX_VALUE;
    private boolean skipBlankRows = true;

    public RowParser(String text, char delimiter) {
        this.text = text;
        this.length = text.length();
        this.delimiter = delimiter;
    }

    public RowParser withMaxRows(int maxRowCount) {
        this.maxRowCount = maxRowCount;
        return this;
    }

    public List<PastedRow> parseAllRows() {
        return parseRows(Integer.MAX_VALUE);
    }

    public List<PastedRow> parseRows(int numberOfRowsToParse) {
        if (numberOfRowsToParse <= 0) {
            throw new IllegalArgumentException("Number of rows to count must be more than 0.");
        }
        List<PastedRow> rows = Lists.newArrayList();
        int count = 0;
        while(hasNextRow() && rows.size() < maxRowCount && count < numberOfRowsToParse) {
            PastedRow parsedRow = readNextLine();
            if (parsedRow != null) {
                rows.add(parsedRow);
                count++;
            }
        }
        return rows;
    }

    public boolean hasNextRow() {
        return !eof();
    }

    private PastedRow readNextLine() {
        List<Integer> offsets = Lists.newArrayList();
        offsets.add(currentPos);
        while(advanceToNextColumn()) {
            offsets.add(currentPos);
        }
        offsets.add(currentPos);

        if (isEmptyRow(offsets)) { // skip if row is empty
            return null;
        }
        return new PastedRow(text, offsets, rowIndex++);
    }

    private boolean isEmptyRow(List<Integer> offsets) {
        final int size = offsets.size();
        if (size > 2) {
            return false;
        } else if (size == 2 && (offsets.get(0) + 1) == offsets.get(1)) {
            return true;
        }
        return false;
    }

    private boolean advanceToNextColumn() {
        if(currentPos >= text.length()) {
            return false;
        }
        if(text.charAt(currentPos) == QUOTE_CHAR) {
            currentPos++;
            return advanceThroughQuotedColumn();
        }
        char c;
        while(true) {
            if(currentPos == length) {
                c = '\n';
                currentPos++; // advance position as if there had been a trailing newline
            } else {
                c = text.charAt(currentPos++);
            }

            if(c == delimiter) {
                return true; // more to come
            } else if(c == '\n') {
                return false;
            }
        }
    }

    private boolean advanceThroughQuotedColumn() {
        while(true) {
            if(currentPos == length) {
                // unterminated quote, handle gracefully
                // advance two characters for the terminating quote
                // and the missing newline
                currentPos = currentPos + 2;
                return false;
            }
            char c = text.charAt(currentPos++);
            if(c == QUOTE_CHAR) {
                // typically quotes withing the column are escaped by being doubled
                // but more generally, we only consider it the end of the column if it's followed
                // by a column or row terminator
                if(currentPos == length) {
                    return false;
                }
                char nextChar = text.charAt(currentPos++);
                if(nextChar == '\n' || nextChar == '\r') {
                    //currentPos++;
                    return false;
                } else if(nextChar == delimiter) {
                    return true;
                }
            }
        }
    }

    public boolean eof() {
        return currentPos >= length;
    }
}
