package org.activityinfo.core.shared.importing.schema;


public class CsvWriter {

    /**
     * Writes a byte order mark that should help spreadsheet software detect the UTF-8 character set.
     */
    public static final char BYTEORDER_MARK = '\ufeff';

    private StringBuilder csv;

    public CsvWriter() {
        this.csv = new StringBuilder();
        csv.append(BYTEORDER_MARK);
    }
    
    private void writeByteOrderMark() {
        csv.append(BYTEORDER_MARK);
    }

    public void writeLine(Object... columns) {

        for (int i = 0; i != columns.length; ++i) {
            if (i > 0) {
                csv.append(",");
            }
            Object val = columns[i];
            if (val != null) {
                if (val instanceof String) {
                    String escaped = ((String) val).replace("\"", "\"\"");
                    csv.append("\"").append(escaped).append("\"");
                } else {
                    csv.append(val.toString());
                }
            }
        }
        csv.append("\n");
    }

    @Override
    public String toString() {
        return csv.toString();
    }
}
