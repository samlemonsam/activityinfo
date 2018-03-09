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

import com.google.common.base.Strings;

import java.util.List;

public class PastedRow implements SourceRow {

    private String source;
    private List<Integer> columnOffsets;
    private int rowIndex;

    public PastedRow(String source, List<Integer> columnOffsets, int rowIndex) {
        super();
        this.source = source;
        this.columnOffsets = columnOffsets;
        this.rowIndex = rowIndex;
    }

    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public String getColumnValue(int columnIndex) {
        try {
            int start = columnOffsets.get(columnIndex);
            int end = columnOffsets.get(columnIndex + 1) - 1;

            if(source.charAt(start) == RowParser.QUOTE_CHAR) {
                return parseQuotedValue(start+1, end);
            }

            return source.substring(start, end);
        } catch(IndexOutOfBoundsException e) {
            return "";
        }
    }

    @Override
    public boolean isColumnValueMissing(int columnIndex) {
        return Strings.isNullOrEmpty(getColumnValue(columnIndex));
    }

    private String parseQuotedValue(int start, int end) {
        if(source.charAt(end-1) == '\r' || source.charAt(end-1) == '\n') {
            end--;
        }
        if(source.charAt(end-1) == RowParser.QUOTE_CHAR) {
            end--;
        }
        String quote = "" + RowParser.QUOTE_CHAR;
        String escapedQuote = quote + quote;
        return source.substring(start, end).replace(quote, escapedQuote);
    }

    public int getColumnCount() {
        return columnOffsets.size()-1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i!=getColumnCount();++i) {
            if(sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(getColumnValue(i).replace("\n", "\\n"));
        }
        return sb.toString();
    }
}
