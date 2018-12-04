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
package org.activityinfo.io.csv;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class CsvWriter implements AutoCloseable {

    /**
     * Writes a byte order mark that should help spreadsheet software detect the UTF-8 character set.
     */
    public static final char BYTEORDER_MARK = '\ufeff';

    public static final String DELIMITER = ",";
    public static final String DOUBLE_QUOTE = "\"";
    public static final String CARRIAGE_RETURN = "\r";
    public static final String LINE_ENDING = "\n";

    public static final String ESCAPED_DOUBLE_QUOTE = "\"\"";

    private Writer writer;

    public CsvWriter() throws IOException {
        this.writer = new StringWriter();
        writeByteOrderMark();
    }

    public CsvWriter(Writer writer) throws IOException {
        this.writer = writer;
        writeByteOrderMark();
    }
    
    private void writeByteOrderMark() throws IOException {
        writer.append(BYTEORDER_MARK);
    }

    public void writeLine(Object... columns) throws IOException {
        for (int i = 0; i != columns.length; ++i) {
            if (i > 0) {
                writer.append(DELIMITER);
            }
            Object val = columns[i];
            if (val != null) {
                if (val instanceof String) {
                    String escaped = escape((String) val);
                    writer.append(enquote(escaped));
                } else {
                    writer.append(val.toString());
                }
            }
        }
        writer.append(LINE_ENDING);
    }

    private String escape(String data) {
        return data.replace(DOUBLE_QUOTE, ESCAPED_DOUBLE_QUOTE);
    }

    private String enquote(String data) {
        return DOUBLE_QUOTE + data + DOUBLE_QUOTE;
    }

    @Override
    public String toString() {
        return writer.toString();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
