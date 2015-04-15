package org.activityinfo.server.query;

import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonWriter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.impl.ColumnSetBuilder;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a JSON response
 */
public class JsonResponseBuilder {

    private ColumnSetBuilder builder;
    private ResourceId rootFormClassId;
    private QueryModel model = new QueryModel(rootFormClassId);
    private List<OutputComponent> writers = new ArrayList<>();

    public JsonResponseBuilder(ResourceId rootFormClassId) {
        this.rootFormClassId = rootFormClassId;
    }

    public void addLegacyId(String fieldName) {

    }

    public void addField(String fieldName, String expression) {
        String columnId = expression;
        writers.add(new FieldOutput(fieldName, columnId));
    }

    private interface OutputWriter {
        void write(JsonWriter writer, int rowIndex) throws IOException;
    }

    private interface OutputComponent {
        OutputWriter createWriter(ColumnSet columns);
    }


    private static class FieldOutput implements OutputComponent {
        private String name;
        private String columnId;

        public FieldOutput(String name, String columnId) {
            this.name = name;
            this.columnId = columnId;
        }

        @Override
        public OutputWriter createWriter(ColumnSet columns) {
            ColumnView view = columns.getColumnView(columnId);
            return new FieldWriter(name, view);
        }
    }

    private class LegacyIdOutput implements OutputComponent {
        private String name;

        public LegacyIdOutput(String name) {
            this.name = name;
            model.selectResourceId().setId("_id");
        }

        @Override
        public OutputWriter createWriter(ColumnSet columns) {
            ColumnView id = Preconditions.checkNotNull(columns.getColumnView("_id"));
            return new FieldWriter(name, id);
        }
    }

    private static class FieldWriter implements OutputWriter {
        private String fieldName;
        private ColumnView view;

        public FieldWriter(String fieldName, ColumnView view) {
            this.fieldName = fieldName;
            this.view = view;
        }

        @Override
        public void write(JsonWriter writer, int rowIndex) throws IOException {
            String value = view.getString(rowIndex);
            if(value != null) {
                writer.name(fieldName);
                writer.value(value);
            }
        }
    }

    private static class LegacyIdWriter implements OutputWriter {

        @Override
        public void write(JsonWriter writer, int rowIndex) throws IOException {
        }
    }

    private static class JsonOutput implements StreamingOutput {

        @Override
        public void write(OutputStream output) throws IOException, WebApplicationException {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(output));


        }
    }
}
