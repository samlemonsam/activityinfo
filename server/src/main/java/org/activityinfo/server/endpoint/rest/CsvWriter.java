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
package org.activityinfo.server.endpoint.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class CsvWriter implements AutoCloseable {

    /**
     * Writes a byte order mark that should help spreadsheet software detect the UTF-8 character set.
     */
    public static final char BYTEORDER_MARK = '\ufeff';

    private Writer writer;

    public CsvWriter() throws IOException {
        this.writer = new StringWriter();
        writer.append(BYTEORDER_MARK);
    }

    public CsvWriter(Writer writer) throws IOException {
        this.writer = writer;
        this.writer.append(BYTEORDER_MARK);
    }
    
    private void writeByteOrderMark() throws IOException {
        writer.append(BYTEORDER_MARK);
    }

    public void writeLine(Object... columns) throws IOException {

        for (int i = 0; i != columns.length; ++i) {
            if (i > 0) {
                writer.append(",");
            }
            Object val = columns[i];
            if (val != null) {
                if (val instanceof String) {
                    String escaped = ((String) val).replace("\"", "\"\"");
                    writer.append("\"").append(escaped).append("\"");
                } else {
                    writer.append(val.toString());
                }
            }
        }
        writer.append("\n");
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
