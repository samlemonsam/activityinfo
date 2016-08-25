package org.activityinfo.server.endpoint.rest;


public class CsvWriter {
    
    private StringBuilder csv;

    public CsvWriter() {
        this.csv = new StringBuilder();
        writeByteOrderMark();
    }
    
    /**
     * Writes a byte order mark that should help spreadsheet software detect the UTF-8 character set.
     */
    private void writeByteOrderMark() {
        csv.append('\ufeff');
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
