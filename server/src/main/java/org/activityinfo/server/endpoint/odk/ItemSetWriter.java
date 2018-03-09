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
package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Charsets;

import java.io.*;

public class ItemSetWriter {
    private final Writer writer;

    // These are defined in
    // https://code.google.com/p/opendatakit/source/browse/src/org/odk/collect/android/external/ExternalSQLiteOpenHelper.java?repo=collect

    private static final char DELIMITING_CHAR = ',';
    private static final char QUOTE_CHAR = '"';
    private static final char ESCAPE_CHAR = '\0';

    ItemSetWriter(OutputStream out) throws IOException {
        this.writer = new OutputStreamWriter(out, Charsets.UTF_8);
        append("list_name,name,label\n");
    }

    public void writeItem(String listName, String name, String label) throws IOException {
        append(listName);
        appendDelimiter();
        append(name);
        appendDelimiter();
        appendEscaped(label);
        appendNewline();
    }

    private void appendEscaped(String label) throws IOException {
        writer.append(QUOTE_CHAR);
        for(int i=0;i!=label.length();++i) {
            char c = label.charAt(i);
            if(c == '\n' || c == '\r') {
                writer.append(' ');
            } else {
                if (c == QUOTE_CHAR) {
                    writer.append(ESCAPE_CHAR);
                }
                writer.append(c);
            }
        }
        writer.append(QUOTE_CHAR);
    }

    private Writer appendDelimiter() throws IOException {
        return writer.append(',');
    }

    private Writer append(String listName) throws IOException {
        return writer.append(listName);
    }

    private Writer appendNewline() throws IOException {
        return writer.append('\n');
    }

    public void flush() throws IOException {
        writer.flush();
    }

}
