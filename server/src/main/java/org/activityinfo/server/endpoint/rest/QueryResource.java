package org.activityinfo.server.endpoint.rest;

import com.google.common.base.Charsets;
import com.sun.jersey.api.core.HttpRequestContext;
import org.activityinfo.model.query.ColumnModelException;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.output.ColumnJsonWriter;
import org.activityinfo.store.spi.FormCatalog;

import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.OutputStream;


public class QueryResource {

    private final Provider<FormCatalog> catalog;

    public QueryResource(Provider<FormCatalog> catalog) {
        this.catalog = catalog;
    }

    @POST
    @Path("columns")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryColumns(@Context Request request) {
        QueryModel model;
        try {
            model = parseModelFromRecord((HttpRequestContext) request);
        } catch (ColumnModelException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
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

    private QueryModel parseModelFromRecord(HttpRequestContext request) {
        String json = request.getEntity(String.class);
        return QueryModel.fromJson(json);
    }
}
