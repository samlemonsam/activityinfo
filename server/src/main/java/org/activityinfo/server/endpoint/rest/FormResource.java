package org.activityinfo.server.endpoint.rest;


import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.annotation.Stringify;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import org.activityinfo.api.client.FormRecordSetBuilder;
import org.activityinfo.io.xlsform.XlsFormBuilder;
import org.activityinfo.json.*;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.model.formTree.JsonFormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.server.command.handler.PermissionOracle;
import org.activityinfo.store.hrd.HrdFormStorage;
import org.activityinfo.store.hrd.HrdSerialNumberProvider;
import org.activityinfo.store.mysql.MySqlCatalog;
import org.activityinfo.store.mysql.RecordHistoryBuilder;
import org.activityinfo.store.query.output.ColumnJsonWriter;
import org.activityinfo.store.query.output.RowBasedJsonWriter;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.FormSupervisorAdapter;
import org.activityinfo.store.query.server.InvalidUpdateException;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.spi.*;

import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.activityinfo.json.impl.JsonUtil.stringify;

public class FormResource {
    public static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

    private static final Logger LOGGER = Logger.getLogger(FormResource.class.getName());

    private final Provider<FormCatalog> catalog;
    private final Provider<AuthenticatedUser> userProvider;
    private final PermissionOracle permissionOracle;
    private BlobAuthorizer blobAuthorizer;

    private final ResourceId formId;

    public FormResource(ResourceId formId,
                        Provider<FormCatalog> catalog,
                        Provider<AuthenticatedUser> userProvider,
                        PermissionOracle permissionOracle,
                        BlobAuthorizer blobAuthorizer) {
        this.formId = formId;
        this.catalog = catalog;
        this.userProvider = userProvider;
        this.permissionOracle = permissionOracle;
        this.blobAuthorizer = blobAuthorizer;
    }

    @GET
    @Produces(JSON_CONTENT_TYPE)
    public Response getMetadataResponse(@QueryParam("localVersion") Long localVersion) {
        FormMetadata metadata = getFormMetadata(localVersion);

        return Response.ok()
                .entity(stringify(metadata.toJsonObject()))
                .type(JSON_CONTENT_TYPE).build();
    }

    private FormMetadata getFormMetadata(Long localVersion) {
        FormMetadata metadata = new FormMetadata();
        metadata.setId(formId);
        Optional<FormStorage> collection = this.catalog.get().getForm(formId);
        if(!collection.isPresent()) {
            metadata.setDeleted(true);
            return metadata;
        }
        FormPermissions permissions = collection.get().getPermissions(userProvider.get().getUserId());
        if(!permissions.isVisible()) {
            metadata.setVisible(false);
            return metadata;
        }

        FormClass schema = collection.get().getFormClass();

        metadata.setVersion(collection.get().cacheVersion());
        metadata.setSchemaVersion(schema.getSchemaVersion());

        if(localVersion == null || localVersion < schema.getSchemaVersion()) {
            metadata.setSchema(schema);
        }

        return metadata;
    }

    /**
     *
     * @return this collection's {@link org.activityinfo.model.form.FormClass}
     */
    @GET
    @Path("schema")
    @Produces(JSON_CONTENT_TYPE)
    public Response getFormSchema() {

        assertVisible(formId);

        FormClass formClass = catalog.get().getFormClass(formId);

        JsonObject object = formClass.toJsonObject();

        return Response.ok(Json.stringify(object, 2)).type(JSON_CONTENT_TYPE).build();
    }

    @POST
    @Path("schema")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postUpdatedFormSchema(String updatedSchemaJson) {
        return updateFormSchema(updatedSchemaJson);
    }
    
    @PUT
    @Path("schema")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateFormSchema(String updatedSchemaJson) {

        FormClass formClass = FormClass.fromJson(updatedSchemaJson);
        
        // Check first to see if this collection exists
        Optional<FormStorage> collection = catalog.get().getForm(formClass.getId());
        if(collection.isPresent()) {
            FormClass existingFormClass = collection.get().getFormClass();
            permissionOracle.assertDesignPrivileges(existingFormClass, userProvider.get());

            collection.get().updateFormClass(formClass);

        } else {
            // Check that we have the permission to create in this database
            permissionOracle.assertDesignPrivileges(formClass, userProvider.get());

            ((MySqlCatalog)catalog.get()).createOrUpdateFormSchema(formClass);
        }
        
        return Response.ok().build();
    }
    
    @GET
    @Path("record/{recordId}")
    @Produces(JSON_CONTENT_TYPE)
    public Response getRecord(@PathParam("recordId") String recordId) {
        
        FormStorage collection = assertVisible(formId);

        Optional<FormRecord> record = collection.get(ResourceId.valueOf(recordId));
        if(!record.isPresent()) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Record " + recordId + " does not exist.")
                    .build();
        }

        return Response.ok()
                .entity(record.get().toJsonElement().toJson())
                .type(JSON_CONTENT_TYPE)
                .build();
    }

    @GET
    @Path("records/versionRange")
    public Response getVersionRange(@QueryParam("localVersion") long localVersion, @QueryParam("version") long version) {
        FormStorage collection = assertVisible(formId);

        List<FormRecord> versionRange;
        if(collection instanceof VersionedFormStorage) {
            versionRange = ((VersionedFormStorage) collection).getVersionRange(localVersion, version);
        } else {
            versionRange = Collections.emptyList();
        }
        return Response.ok()
                .entity(encode(versionRange))
                .type(JSON_CONTENT_TYPE)
                .build();
    }

    @POST
    @Path("record/{recordId}/field/{fieldId}/geometry")
    public Response updateGeometry(@PathParam("recordId") String recordId, @PathParam("fieldId") String fieldId,
                                   byte[] binaryBody) {

        // Parse the Geometry
        WKBReader reader = new WKBReader(new GeometryFactory());
        Geometry geometry;
        try {
            geometry = reader.read(binaryBody);
        } catch (ParseException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Could not parse WKB geometry: " + e.getMessage())
                    .build();
        }

        geometry.normalize();

        if(!geometry.isValid()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(geometry.getGeometryType() + " is not valid")
                    .build();
        }

        // Check first to see if this form exists
        Optional<FormStorage> storage = catalog.get().getForm(formId);
        if(!storage.isPresent()) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Form " + formId + " does not exist.")
                    .build();
        }

        // Find the field and verify that it's a GeoArea type
        FormField field;
        try {
            field = storage.get().getFormClass().getField(ResourceId.valueOf(fieldId));
        } catch (IllegalArgumentException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Record " + recordId + " does not exist.")
                    .build();
        }
        if(!(field.getType() instanceof GeoAreaType)) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Field " + fieldId + " is not a GeoArea type")
                    .build();
        }

        try {
            storage.get().updateGeometry(ResourceId.valueOf(recordId), ResourceId.valueOf(fieldId), geometry);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update geometry for record " + recordId, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }

        return Response.ok().build();
    }

    @GET
    @Path("record/{recordId}/history")
    @Produces(JSON_CONTENT_TYPE)
    public Response getRecordHistory(@PathParam("recordId") String recordId) throws SQLException {

        assertVisible(formId);

        RecordHistoryBuilder builder = new RecordHistoryBuilder((MySqlCatalog) catalog.get());
        JsonArray array = builder.build(formId, ResourceId.valueOf(recordId));

        return Response.ok()
                .entity(array.toJson())
                .type(JSON_CONTENT_TYPE)
                .build();
    }


    @GET
    @Path("records")
    @Produces(JSON_CONTENT_TYPE)
    public Response getRecords(@QueryParam("parentId") String parentId) {

        assertVisible(formId);
        
        Optional<FormStorage> collection = catalog.get().getForm(formId);
        if(!collection.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        HrdFormStorage hrdForm = (HrdFormStorage) collection.get();
        Iterable<FormRecord> records = hrdForm.getSubRecords(ResourceId.valueOf(parentId));

        return Response.ok(encode(records), JSON_CONTENT_TYPE).build();
    }

    private String encode(Iterable<FormRecord> records) {
        FormRecordSetBuilder recordSet = new FormRecordSetBuilder();
        recordSet.setFormId(formId.asString());

        for (FormRecord record : records) {
            recordSet.addRecord(record);
        }
        return recordSet.toJsonString();
    }

    @POST
    @Path("records")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRecord(String body) {
        
        assertVisible(formId);
        
        JsonValue jsonObject = new JsonParser().parse(body);

        Updater updater = new Updater(catalog.get(), userProvider.get().getUserId(), blobAuthorizer,
                new HrdSerialNumberProvider());

        try {
            updater.create(formId, jsonObject.getAsJsonObject());
        } catch (InvalidUpdateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }

    @PUT
    @Path("record/{recordId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRecord(@PathParam("recordId") String recordId, String body) {

        assertVisible(formId);

        JsonValue jsonObject = new JsonParser().parse(body);

        try {
            Updater updater = new Updater(catalog.get(), userProvider.get().getUserId(), blobAuthorizer,
                    new HrdSerialNumberProvider());
            updater.execute(formId, ResourceId.valueOf(recordId), jsonObject.getAsJsonObject());
        } catch (InvalidUpdateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }
    
    @GET
    @Path("class")
    public Response getFormClass() {
        return getFormSchema();
    }

    @GET
    @Path("form.xls")
    public Response getXlsForm() {
        assertVisible(formId);

        final XlsFormBuilder xlsForm = new XlsFormBuilder(catalog.get());
        xlsForm.build(formId);

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

        return Response.ok(Json.stringify(object, 2)).type(JSON_CONTENT_TYPE).build();
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
        assertVisible(formId);

        FormTreeBuilder builder = new FormTreeBuilder(catalog.get());
        return builder.queryTree(formId);
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

        assertVisible(formId);

        final QueryModel queryModel = new QueryModel(formId);
        final FormTreeBuilder treeBuilder = new FormTreeBuilder(catalog.get());
        final FormTree tree = treeBuilder.queryTree(formId);

        if(uriInfo.getQueryParameters().isEmpty()) {
            LOGGER.info("No query fields provided, querying all.");
            queryModel.selectResourceId().as("@id");

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

        ColumnSetBuilder builder = new ColumnSetBuilder(
                catalog.get(),
                new FormSupervisorAdapter(catalog.get(), userProvider.get().getId()));

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

    private FormStorage assertVisible(ResourceId collectionId) {
        Optional<FormStorage> collection = this.catalog.get().getForm(formId);
        if(!collection.isPresent()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(format("Collection %s does not exist.", collectionId.asString()))
                            .build());
        }
        FormPermissions permissions = collection.get().getPermissions(userProvider.get().getUserId());
        if(!permissions.isVisible()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity(format("You do not have permission to view the form %s", collectionId.asString()))
                            .build());

        }
        return collection.get();
    }
}
