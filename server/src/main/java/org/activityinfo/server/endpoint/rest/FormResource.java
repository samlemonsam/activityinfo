package org.activityinfo.server.endpoint.rest;


import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.JsonFormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.server.database.hibernate.HibernateQueryExecutor;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.output.RowBasedJsonWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class FormResource {
    public static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
    
    private final HibernateQueryExecutor queryExecutor;
    private final ResourceId resourceId;
    private final Gson prettyPrintingGson;

    public FormResource(ResourceId resourceId, HibernateQueryExecutor queryExecutor) {
        this.resourceId = resourceId;
        this.queryExecutor = queryExecutor;
        this.prettyPrintingGson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    @GET
    @Path("class")
    public Response getFormClass() {
        FormClass formClass = queryExecutor.doWork(new HibernateQueryExecutor.StoreSession<FormClass>() {
            @Override
            public FormClass execute(CollectionCatalog catalog) {
                return catalog.getFormClass(resourceId);
            }
        });
        
        JsonObject object = Resources.toJsonObject(formClass.asResource());

        return Response.ok(prettyPrintingGson.toJson(object)).type(JSON_CONTENT_TYPE).build();
    }
    
    @GET
    @Path("tree")
    public Response getTree() {
        FormTree tree = queryExecutor.doWork(new HibernateQueryExecutor.StoreSession<FormTree>() {
            @Override
            public FormTree execute(CollectionCatalog catalog) {
                FormTreeBuilder builder = new FormTreeBuilder(catalog);
                return builder.queryTree(resourceId);
            }
        });
        
        JsonObject object = JsonFormTreeBuilder.toJson(tree);
        
        return Response.ok(prettyPrintingGson.toJson(object)).type(JSON_CONTENT_TYPE).build();
    }
    
    @GET
    @Path("query/rows")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryColumns(@Context UriInfo uriInfo) {
        final QueryModel queryModel = new QueryModel(resourceId);
        for (String columnId : uriInfo.getQueryParameters().keySet()) {
            queryModel.selectExpr(uriInfo.getQueryParameters().getFirst(columnId)).as(columnId);
        }
        final ColumnSet columnSet = queryExecutor.doWork(new HibernateQueryExecutor.StoreSession<ColumnSet>() {
            @Override
            public ColumnSet execute(CollectionCatalog catalog) {
                ColumnSetBuilder builder = new ColumnSetBuilder(catalog, ColumnCache.NULL);
                return builder.build(queryModel);
            }
        });

        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream, Charsets.UTF_8);
                JsonWriter jsonWriter = new JsonWriter(outputWriter);
                RowBasedJsonWriter columnSetWriter = new RowBasedJsonWriter(jsonWriter);
                columnSetWriter.write(columnSet);
                jsonWriter.flush();
            }
        };

        return Response.ok(output).type(JSON_CONTENT_TYPE).build();
        
    }
}
