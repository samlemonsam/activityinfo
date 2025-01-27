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
import org.activityinfo.model.query.ConstantColumnView;
import org.activityinfo.model.query.EmptyColumnView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

public class ColumnJsonWriter {
    
    private final JsonWriter writer;

    public ColumnJsonWriter(JsonWriter writer) {
        this.writer = writer;
    }

    public ColumnJsonWriter(OutputStream outputStream, Charset charset) {
        this(new JsonWriter(new OutputStreamWriter(outputStream, charset)));
    }

    public void write(ColumnSet result) throws IOException {
        
        writer.beginObject();
        writer.name("rows").value(result.getNumRows());
        writeColumns(result);

        writer.endObject();
        writer.flush();
    }

    private void writeColumns(ColumnSet result) throws IOException {
        writer.name("columns");
        writer.beginObject();
        for (Map.Entry<String, ColumnView> column : result.getColumns().entrySet()) {
            writer.name(column.getKey());
            writeColumn(column.getValue());
        }
        writer.endObject();
    }


    private void writeColumn(ColumnView view) throws IOException {
        writer.beginObject();
        writer.name("type").value(view.getType().name());
        
        if (view instanceof EmptyColumnView) {
            writer.name("storage").value("empty");

        } else if (view instanceof ConstantColumnView) {
            writeConstantView(view);

        } else {
            writeArrayView(view);
        }
        writer.endObject();
    }

    private void writeConstantView(ColumnView view) throws IOException {
        writer.name("storage").value("constant");

        switch (view.getType()) {
            case STRING:
                writer.name("value").value(view.getString(0));
                break;
            case NUMBER:
                writer.name("value").value(view.getDouble(0));
                break;
            case BOOLEAN:
                writer.name("value").value(view.getBoolean(0));
        }
    }

    private void writeArrayView(ColumnView view) throws IOException {
        writer.name("storage").value("array");
        writer.name("values");
        writer.beginArray();
        switch(view.getType()) {
            case STRING:
                writeStringValues(view);
                break;
            case NUMBER:
                writeDoubleValues(view);
                break;
            case BOOLEAN:
                writeBooleanValues(view);
                break;
        }
        writer.endArray();
    }

    private void writeBooleanValues(ColumnView view) throws IOException {
        for (int i = 0; i < view.numRows(); i++) {
            writer.value(view.getBoolean(i));
        }
    }

    private void writeStringValues(ColumnView view) throws IOException {
        for(int i=0;i<view.numRows();++i) {
            writer.value(view.getString(i));
        }
    }

    private void writeDoubleValues(ColumnView view) throws IOException {
        for(int i=0;i<view.numRows();++i) {
            double x = view.getDouble(i);
            if(Double.isNaN(x)) {
                writer.nullValue();
            } else {
                writer.value(x);
            }
        }
    }
    
    public void flush() throws IOException {
        writer.flush();
    }
}
