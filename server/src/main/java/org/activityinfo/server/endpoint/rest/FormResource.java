package org.activityinfo.server.endpoint.rest;


import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.*;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.output.ColumnJsonWriter;
import org.activityinfo.store.query.output.RowBasedJsonWriter;
import org.activityinfo.xlsform.XlsFormBuilder;

import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import static java.lang.String.format;

public class FormResource {
    public static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

    private static final Logger LOGGER = Logger.getLogger(FormResource.class.getName());

    private final Provider<CollectionCatalog> catalog;
    private final Provider<AuthenticatedUser> userProvider;

    private final ResourceId resourceId;
    private final Gson prettyPrintingGson;
    private final FormClassProvider formClassProvider;

    public FormResource(ResourceId resourceId, 
                        Provider<CollectionCatalog> catalog, 
                        Provider<AuthenticatedUser> userProvider, 
                        FormClassProvider formClassProvider) {
        this.resourceId = resourceId;
        this.catalog = catalog;
        this.userProvider = userProvider;
        this.prettyPrintingGson = new GsonBuilder().setPrettyPrinting().create();
        this.formClassProvider = formClassProvider;
    }

    /**
     *
     * @return this collection's {@link org.activityinfo.model.form.FormClass}
     */
    @GET
    @Path("class")
    public Response getFormClass() {

        assertVisible(resourceId);

        FormClass formClass = catalog.get().getFormClass(resourceId);

        JsonObject object = Resources.toJsonObject(formClass.asResource());

        return Response.ok(prettyPrintingGson.toJson(object)).type(JSON_CONTENT_TYPE).build();
    }

    @GET
    @Path("form.xls")
    public Response getXlsForm() {
        assertVisible(resourceId);

        final XlsFormBuilder xlsForm = new XlsFormBuilder(formClassProvider);
        xlsForm.build(resourceId);

        StreamingOutput output = new StreamingOutput() {

            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                xlsForm.write(outputStream);
            }
        };
        
        return Response.ok(output, "application/vnd.ms-excel").build();
    }
    
    /**
     *
     * @return a list of {@link org.activityinfo.model.form.FormClass}es that includes the {@code FormClass}
     * of this collection and any {@code FormClass}es reachable from this collection's fields.
     */
    @GET
    @Path("tree")
    public Response getTree() {

        FormTree tree = fetchTree();
        JsonObject object = JsonFormTreeBuilder.toJson(tree);

        return Response.ok(prettyPrintingGson.toJson(object)).type(JSON_CONTENT_TYPE).build();
    }

    @GET
    @Path("tree/pretty")
    public Response getTreePrettyPrinted() {

        FormTree tree = fetchTree();
        StringWriter stringWriter = new StringWriter();
        FormTreePrettyPrinter printer = new FormTreePrettyPrinter(new PrintWriter(stringWriter));
        printer.printTree(tree);

        return Response.ok(stringWriter.toString()).type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    private FormTree fetchTree() {
        assertVisible(resourceId);

        FormTreeBuilder builder = new FormTreeBuilder(catalog.get());
        return builder.queryTree(resourceId);
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

        assertVisible(resourceId);

        final QueryModel queryModel = new QueryModel(resourceId);
        if(uriInfo.getQueryParameters().isEmpty()) {
            LOGGER.info("No query fields provided, querying all.");
            queryModel.selectResourceId().as("@id");
            FormTreeBuilder treeBuilder = new FormTreeBuilder(catalog.get());
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

        ColumnSetBuilder builder = new ColumnSetBuilder(catalog.get());
        return builder.build(queryModel);
    }

    private boolean includeInDefaultQuery(FormTree.Node leaf) {
        FieldType type = leaf.getType();
        return type instanceof TextType ||
                type instanceof BarcodeType ||
                type instanceof QuantityType ||
                type instanceof EnumType ||
                type instanceof ReferenceType ||
                type instanceof BooleanType ||
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

    private void assertVisible(ResourceId collectionId) {
        Optional<ResourceCollection> collection = this.catalog.get().getCollection(resourceId);
        if(!collection.isPresent()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(format("Collection %s does not exist.", collectionId.asString()))
                            .build());
        }
        CollectionPermissions permissions = collection.get().getPermissions(userProvider.get().getUserId());
        if(!permissions.isVisible()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity(format("You do not have permission to view the collection %s", collectionId.asString()))
                            .build());

        }
    }
}
