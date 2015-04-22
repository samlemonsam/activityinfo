package org.activityinfo.server.endpoint.rest;

import com.google.common.base.Charsets;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.server.database.hibernate.HibernateQueryExecutor;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.output.ColumnJsonWriter;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;


public class QueryResource {

    private final HibernateQueryExecutor queryExecutor;

    public QueryResource(HibernateQueryExecutor queryExecutor) {

        this.queryExecutor = queryExecutor;
    }

    @POST
    @Path("columns")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryColumns(final QueryModel model) {

        final ColumnSet columnSet = queryExecutor.doWork(new HibernateQueryExecutor.StoreSession<ColumnSet>() {
            @Override
            public ColumnSet execute(CollectionCatalog catalog) {
                ColumnSetBuilder builder = new ColumnSetBuilder(catalog, ColumnCache.NULL);
                return builder.build(model);
            }
        });
        
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
