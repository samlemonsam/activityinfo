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
