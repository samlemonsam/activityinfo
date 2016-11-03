package org.activityinfo.store.query.output;

import com.google.gson.stream.JsonWriter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.ConstantColumnView;
import org.activityinfo.store.query.impl.views.EmptyColumnView;
import org.activityinfo.store.query.impl.views.GeoColumnView;

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

        } else if (view instanceof GeoColumnView) {
            writeCoordinates((GeoColumnView) view);
            
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
    
    private void writeCoordinates(GeoColumnView view) throws IOException {
        writer.name("storage").value("coordinates");
        writer.name("coordinates");
        writer.beginArray();
        for (int i = 0; i < view.numCoordinates(); i++) {
            double coordinate = view.getCoordinate(i);
            if(Double.isNaN(coordinate)) {
                writer.nullValue();
            } else {
                writer.value(coordinate);
            }
        }
        writer.endArray();
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
