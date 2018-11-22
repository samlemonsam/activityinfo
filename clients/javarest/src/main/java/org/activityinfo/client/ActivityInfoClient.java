/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonParser;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.*;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.activityinfo.json.Json.parse;

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

        JsonValue properties = Json.createObject();
        properties.put("name", databaseName);

        JsonValue createEntity = Json.createObject();
        createEntity.put("entityName", "UserDatabase");
        createEntity.add("properties", properties);

        JsonValue command = Json.createObject();
        command.put("type", "CreateEntity");
        command.add("command", createEntity);

        String result = client.resource(root).path("command").post(String.class, command.toJson());
        JsonValue resultObject = (JsonValue) parser.parse(result);

        int newId = resultObject.get("newId").asInt();

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

    public void createRecord(TypedFormRecord record) {
        ClientResponse response = client.resource(root)
                .path("resources")
                .path("form")
                .path(record.getFormId().asString())
                .path("record")
                .path(record.getId().asString())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class, record.toJsonObject().toJson());

        if(response.getStatus() == 400) {
            throw new IllegalArgumentException(response.getEntity(String.class));
        } else if(response.getStatus() != 200) {
            throw new RuntimeException(response.getEntity(String.class));
        }
    }


    public void update(RecordTransaction tx) {
        ClientResponse response = client.resource(root)
            .path("resources")
            .path("update")
            .type(MediaType.APPLICATION_JSON_TYPE)
            .post(ClientResponse.class, Json.toJson(tx).toJson());

        if(response.getStatus() != 200) {
            throw new RuntimeException(response.getEntity(String.class));
        }
    }


    public FormClass getFormSchema(ResourceId formId) {
        String json = client.resource(root)
                .path("resources")
                .path("form")
                .path(formId.asString())
                .path("schema")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);


        JsonValue jsonObject = parse(json);
        FormClass formClass = FormClass.fromJson(jsonObject);

        return formClass;
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

        JsonValue jsonObject = (JsonValue) parser.parse(json);

        return FormRecord.fromJson(jsonObject);
    }

    public TypedFormRecord getTypedRecord(FormClass formClass, ResourceId recordId) {
        FormRecord record = getRecord(formClass.getId(), recordId);
        TypedFormRecord instance = TypedFormRecord.toTypedFormRecord(formClass, record);
        return instance;
    }

    public ColumnSet queryTable(QueryModel queryModel) {

        String json = client.resource(root)
                .path("resources")
                .path("query")
                .path("columns")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(String.class, queryModel.toJsonString());

        JsonValue object = (JsonValue) parser.parse(json);
        int numRows = object.get("rows").asInt();

        Map<String, ColumnView> columnMap = new HashMap<>();
        for (Map.Entry<String, JsonValue> column : object.get("columns").entrySet()) {
            JsonValue columnValue = column.getValue();
            String storage = columnValue.get("storage").asString();
            switch (storage) {
                case "array":
                    columnMap.put(column.getKey(), new ColumnViewWrapper(numRows, columnValue.get("values")));
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

    private ColumnView parseConstantColumn(int numRows, JsonValue columnValue) {
        if(columnValue.get("value").isJsonNull()) {
            return parseEmpty(numRows, columnValue);
        }
        String typeName = columnValue.get("type").asString();
        switch(typeName) {
            case "STRING":
                return new ConstantColumnView(numRows, columnValue.get("value").asString());
            case "NUMBER":
                return new ConstantColumnView(numRows, columnValue.get("value").asNumber());
            case "BOOLEAN":
                return new ConstantColumnView(numRows, columnValue.get("value").asBoolean());
            default:
                throw new UnsupportedOperationException("type: " + typeName);
        }
    }


    private ColumnView parseEmpty(int numRows, JsonValue columnValue) {
        String typeName = columnValue.get("type").asString();
        ColumnType type = ColumnType.valueOf(typeName);
        return new EmptyColumnView(type, numRows);
    }

}
