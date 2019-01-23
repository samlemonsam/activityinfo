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
package org.activityinfo.store.query.output;

import com.google.gson.stream.JsonWriter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Writes a {@code ColumnSet} to an array of JSON Objects
 */
public class RowBasedJsonWriter {

    private final JsonWriter writer;

    public RowBasedJsonWriter(JsonWriter writer) {
        this.writer = writer;
    }

    public RowBasedJsonWriter(Writer writer) {
        this.writer = new JsonWriter(writer);
    }

    public RowBasedJsonWriter(OutputStream outputStream, Charset charset) {
        this.writer = new JsonWriter(new OutputStreamWriter(outputStream, charset));
    }

    public void write(ColumnSet columnSet) throws IOException {

        int numRows = columnSet.getNumRows();
        int numCols = columnSet.getColumns().size();
        FieldWriter[] writers = createWriters(columnSet);

        writer.beginArray();
        for(int rowIndex=0;rowIndex<numRows;++rowIndex) {

            writer.beginObject();
            for(int colIndex=0;colIndex!=numCols;++colIndex) {
                writers[colIndex].write(rowIndex);
            }
            writer.endObject();
        }
        writer.endArray();
    }

    public void flush() throws IOException {
        writer.flush();
    }

    private interface FieldWriter {
        void write(int rowIndex) throws IOException;
    }
    
    private FieldWriter[] createWriters(ColumnSet columnSet) {
        FieldWriter[] writers = new FieldWriter[columnSet.getColumns().size()];
        int index = 0;
        for (Map.Entry<String, ColumnView> column : columnSet.getColumns().entrySet()) {
            writers[index] = createWriter(column.getKey(), column.getValue());
            index++;
        }
        return writers;
    }

    public FieldWriter createWriter(final String id, final ColumnView view) {

        switch(view.getType()) {
            case STRING:
                return new FieldWriter() {
                    @Override
                    public void write(int rowIndex) throws IOException {
                        String value = view.getString(rowIndex);
                        writer.name(id);
                        if (value == null) {
                            writer.nullValue();
                        } else {
                            writer.value(view.getString(rowIndex));
                        }
                    }
                };

            case NUMBER:
                return new FieldWriter() {
                    @Override
                    public void write(int rowIndex) throws IOException {
                        double value = view.getDouble(rowIndex);
                        writer.name(id);
                        if (Double.isNaN(value)) {
                            writer.nullValue();
                        } else {
                            writer.value(value);
                        }
                    }
                };
            case BOOLEAN:
                return new FieldWriter() {
                    @Override
                    public void write(int rowIndex) throws IOException {
                        int value = view.getBoolean(rowIndex);
                        writer.name(id);
                        if (value == ColumnView.NA) {
                            writer.nullValue();
                        } else {
                            writer.value(value != 0);
                        }
                    }
                };
        }
        throw new IllegalArgumentException("type: " + view.getType());
    }
}
