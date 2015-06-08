package org.activityinfo.server.endpoint.rest;


import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.JsonFormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.server.database.hibernate.HibernateQueryExecutor;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.output.ColumnJsonWriter;
import org.activityinfo.store.query.output.RowBasedJsonWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class FormResource {
    public static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
    
    private static final Logger LOGGER = Logger.getLogger(FormResource.class.getName());
    
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
    public Response queryRows(@Context UriInfo uriInfo) {
        final ColumnSet columnSet = query(uriInfo);
        
        LOGGER.info("Query completed with " + columnSet.getNumRows() + " rows.");

        final StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                RowBasedJsonWriter writer = new RowBasedJsonWriter(outputStream, Charsets.UTF_8);
                writer.write(columnSet);
                writer.flush();
            }
        };

        return Response.ok(output).type(JSON_CONTENT_TYPE).build();
    }

    @GET
    @Path("query/columns")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryColumns(@Context UriInfo uriInfo) {
        final ColumnSet columnSet = query(uriInfo);

        final StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                ColumnJsonWriter columnSetWriter = new ColumnJsonWriter(outputStream, Charsets.UTF_8);
                columnSetWriter.write(columnSet);
                columnSetWriter.flush();
            }
        };

        return Response.ok(output).type(JSON_CONTENT_TYPE).build();
    }



    private ColumnSet query(final UriInfo uriInfo) {
  
        return queryExecutor.doWork(new HibernateQueryExecutor.StoreSession<ColumnSet>() {
            @Override
            public ColumnSet execute(CollectionCatalog catalog) {

                final QueryModel queryModel = new QueryModel(resourceId);
                if(uriInfo.getQueryParameters().isEmpty()) {
                    LOGGER.info("No query fields provided, querying all.");
                    FormTreeBuilder treeBuilder = new FormTreeBuilder(catalog);
                    FormTree tree = treeBuilder.queryTree(resourceId);
                    for (FormTree.Node leaf : tree.getLeaves()) {
                        if(includeInDefaultQuery(leaf)) {
                            queryModel.selectField(leaf.getPath()).as(formatId(leaf));
                        }
                    }
                    LOGGER.info("Query model: " + queryModel);

                } else {
                    for (String columnId : uriInfo.getQueryParameters().keySet()) {
                        queryModel.selectExpr(uriInfo.getQueryParameters().getFirst(columnId)).as(columnId);
                    }
                }
                
                ColumnSetBuilder builder = new ColumnSetBuilder(catalog, ColumnCache.NULL);
                return builder.build(queryModel);
            }
        });
    }

    private boolean includeInDefaultQuery(FormTree.Node leaf) {
        FieldType type = leaf.getType();
        return type instanceof TextField ||
               type instanceof BarcodeType ||
               type instanceof QuantityType ||
               type instanceof EnumType ||
               type instanceof ReferenceType ||
               type instanceof LocalDateType;
    }

    private String formatId(FormTree.Node node) {
        StringBuilder id = new StringBuilder();
        id.append(formatIdField(node));
        FormTree.Node parent = node.getParent();
        while(parent != null) {
            id.insert(0, formatIdField(parent) + ".");
            parent = parent.getParent();
        }
        return id.toString();
    }

    private String formatIdField(FormTree.Node node) {
        return node.getField().getCode() == null ? node.getField().getLabel() : node.getField().getCode();
    }
}
