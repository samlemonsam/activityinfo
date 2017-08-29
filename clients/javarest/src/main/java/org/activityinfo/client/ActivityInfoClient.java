package org.activityinfo.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.*;
import org.activityinfo.model.resource.ResourceId;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard Java REST Client for ActivityInfo
 */
public class ActivityInfoClient {

    private Client client;
    private URI root;

    private JsonParser parser = new JsonParser();

    public ActivityInfoClient(String endpoint, String username, String password) {
        ClientConfig clientConfig = new DefaultClientConfig();

        client = Client.create(clientConfig);
        client.addFilter(new HTTPBasicAuthFilter(username, password));

        root = UriBuilder.fromUri(endpoint).build();
    }


    public ResourceId createDatabase(String databaseName) {

        JsonObject properties = new JsonObject();
        properties.addProperty("name", databaseName);

        JsonObject createEntity = new JsonObject();
        createEntity.addProperty("entityName", "UserDatabase");
        createEntity.add("properties", properties);

        JsonObject command = new JsonObject();
        command.addProperty("type", "CreateEntity");
        command.add("command", createEntity);

        String result = client.resource(root).path("command").post(String.class, command.toString());
        JsonObject resultObject = (JsonObject) parser.parse(result);

        int newId = resultObject.get("newId").getAsInt();

        return CuidAdapter.databaseId(newId);
    }

    public void createForm(FormClass formClass) {

        client.resource(root)
                .path("resources")
                .path("form")
                .path(formClass.getId().asString())
                .path("schema")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(formClass.toJsonString());
    }

    public void createRecord(FormInstance record) {
        ClientResponse response = client.resource(root)
                .path("resources")
                .path("form")
                .path(record.getFormId().asString())
                .path("record")
                .path(record.getId().asString())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class, record.toJsonObject().toString());

        if(response.getStatus() == 400) {
            throw new IllegalArgumentException(response.getEntity(String.class));
        } else if(response.getStatus() != 200) {
            throw new RuntimeException(response.getEntity(String.class));
        }
    }

    public FormRecord getRecord(ResourceId formId, ResourceId recordId) {

        String json = client.resource(root)
                .path("resources")
                .path("form")
                .path(formId.asString())
                .path("record")
                .path(recordId.asString())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        JsonObject jsonObject = (JsonObject) parser.parse(json);

        return FormRecord.fromJson(jsonObject);
    }

    public FormInstance getTypedRecord(FormClass formClass, ResourceId recordId) {
        FormRecord record = getRecord(formClass.getId(), recordId);
        FormInstance instance = FormInstance.toFormInstance(formClass, record);
        return instance;
    }

    public ColumnSet queryTable(QueryModel queryModel) {

        String json = client.resource(root)
                .path("query")
                .path("columns")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(String.class, queryModel.toJsonString());

        JsonObject object = (JsonObject) parser.parse(json);
        int numRows = object.getAsJsonPrimitive("rows").getAsInt();

        Map<String, ColumnView> columnMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> column : object.getAsJsonObject("columns").entrySet()) {
            JsonObject columnValue = column.getValue().getAsJsonObject();
            String storage = columnValue.getAsJsonPrimitive("storage").getAsString();
            switch (storage) {
                case "array":
                    columnMap.put(column.getKey(), new ColumnViewWrapper(numRows, columnValue.getAsJsonArray("values")));
                    break;
                case "empty":
                    columnMap.put(column.getKey(), parseEmpty(numRows, columnValue));
                    break;
                case "constant":
                    columnMap.put(column.getKey(), parseConstantColumn(numRows, columnValue));
                    break;

                default:
                    throw new UnsupportedOperationException(storage);
            }
        }

        return new ColumnSet(numRows, columnMap);
    }

    private ColumnView parseConstantColumn(int numRows, JsonObject columnValue) {
        if(columnValue.get("value").isJsonNull()) {
            return parseEmpty(numRows, columnValue);
        }
        String typeName = columnValue.get("type").getAsString();
        switch(typeName) {
            case "STRING":
                return new ConstantColumnView(numRows, columnValue.get("value").getAsString());
            case "NUMBER":
                return new ConstantColumnView(numRows, columnValue.get("value").getAsDouble());
            case "BOOLEAN":
                return new ConstantColumnView(numRows, columnValue.get("value").getAsBoolean());
            default:
                throw new UnsupportedOperationException("type: " + typeName);
        }
    }


    private ColumnView parseEmpty(int numRows, JsonObject columnValue) {
        String typeName = columnValue.get("type").getAsString();
        ColumnType type = ColumnType.valueOf(typeName);
        return new EmptyColumnView(type, numRows);
    }

}
