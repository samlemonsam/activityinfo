package org.activityinfo.server.endpoint.rest;

import com.google.common.base.Charsets;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.output.ColumnJsonWriter;

import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;


public class QueryResource {

    private final Provider<CollectionCatalog> catalog;

    public QueryResource(Provider<CollectionCatalog> catalog) {
        this.catalog = catalog;
    }

    @POST
    @Path("columns")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryColumns(final QueryModel model) {

        ColumnSetBuilder builder = new ColumnSetBuilder(catalog.get());
        final ColumnSet columnSet = builder.build(model);

        final StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                ColumnJsonWriter columnSetWriter = new ColumnJsonWriter(outputStream, Charsets.UTF_8);
                columnSetWriter.write(columnSet);
                columnSetWriter.flush();
            }
        };

        return Response.ok(output).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
