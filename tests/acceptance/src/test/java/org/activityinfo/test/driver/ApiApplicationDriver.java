package org.activityinfo.test.driver;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.activityinfo.test.sut.*;
import org.eclipse.jetty.util.ajax.JSON;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class ApiApplicationDriver implements ApplicationDriver {

    private final Server server;
    private final Accounts accounts;
    
    private final Client client;

    private final AliasTable aliases;
    private final WebResource endpoint;

    @Inject
    public ApiApplicationDriver(Server server, Accounts accounts) {
        this.server = server;
        this.accounts = accounts;
        this.aliases = new AliasTable();
        
        client = new Client();
        endpoint = client.resource(server.getRootUrl()).path("command");
    }

    @Override
    public void login() {
        UserAccount account = accounts.any();
        client.addFilter(new HTTPBasicAuthFilter(account.getEmail(), account.getPassword()));
    }


    @Override
    public void createDatabase(Property... databaseProperties) throws Exception {
        TestProps map = new TestProps(databaseProperties);

        String name = map.getString("name");
        JSONObject properties = new JSONObject();
        properties.put("name", aliases.create(name));
        properties.put("countryId", 1);

        createEntity("UserDatabase", name, properties);
    }


    @Override
    public void createForm(Property... arguments) throws Exception {
        TestProps map = new TestProps(arguments);
        
        String name = map.getString("name");
        JSONObject properties = new JSONObject();
        properties.put("name", aliases.create(name));
        properties.put("databaseId", aliases.getId(map.getString("database")));
        properties.put("locationTypeId", 50529); // TODO: de-hardcode (this is Rdc/Country)
        createEntity("Activity", name, properties);
    }

    @Override
    public void createField(Property... arguments) throws Exception {
        TestProps map = new TestProps(arguments);

        String name = map.getString("name");
        JSONObject properties = new JSONObject();
        properties.put("name", aliases.create(name));
        properties.put("activityId", aliases.getId(map.getString("form")));
        properties.put("type", map.getString("type"));
        properties.put("units", map.getString("units", "parsects"));

        createEntity("Indicator", name, properties);
    }

    @Override
    public void submitForm(String formName, List<FieldValue> values) throws Exception {
        int activityId = aliases.getId(formName);

        JSONObject properties = new JSONObject();
        properties.put("activityId", activityId);
        properties.put("locationId", 50529); // TODO: dehard code
        properties.put("id", aliases.generateId());
        properties.put("date1", "2014-01-01");
        properties.put("date2", "2014-02-01");
        
        for(FieldValue value : values) {
            if(value.getField().equals("partner")) {
                properties.put("partnerId", aliases.getId(value.getValue()));
            } else {
                int indicatorId = aliases.getId(value.getField());
                properties.put("I" + indicatorId, value.maybeNumberValue());
            }
        }

        JSONObject command = new JSONObject();
        command.put("properties", properties);
    
        executeCommand("CreateSite", command);
    }

    @Override
    public void addPartner(String partnerName, String databaseName) throws Exception {
        int databaseId = aliases.getId(databaseName);

        JSONObject partner = new JSONObject();
        partner.put("name", aliases.create(partnerName));
        
        JSONObject command = new JSONObject();
        command.put("databaseId", databaseId);
        command.put("partner", partner);

        JSONObject response = executeCommand("AddPartner", command);
        
        int partnerId = response.getInt("newId");

        aliases.mapId(partnerName, partnerId);
    }

    @Override
    public void createTarget(Property... arguments) throws Exception {
        TestProps properties = new TestProps(arguments);

        String name = aliases.create(properties.getString("name"));
        
        JSONObject target = new JSONObject();
        target.put("name", name);

        JSONObject addTarget = new JSONObject();
        addTarget.put("databaseId", aliases.getId(properties.getString("database")));
        addTarget.put("target", target);

        JSONObject response = executeCommand("AddTarget", addTarget);
        
        int id = response.getInt("newId");
        aliases.mapId(name, id);
    }

    @Override
    public void setTargetValues(String targetName, List<FieldValue> values) {
        
  
        
    }

    private void createEntity(String entityType, String alias, JSONObject properties) throws JSONException {

        JSONObject object = new JSONObject();
        object.put("entityName", entityType);
        object.put("properties", properties);

        JSONObject response = executeCommand("CreateEntity", object);

        int newId = response.getInt("newId");
        aliases.mapId(alias, newId);
    }

    private JSONObject executeCommand(String type, JSONObject command) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("type", type);
        request.put("command", command);

        String response = null;
        try {
            response = endpoint.type(MediaType.APPLICATION_JSON_TYPE).post(String.class, request.toString());
        } catch (UniformInterfaceException e) {
            throw new RuntimeException("400: " + e.getResponse().getEntity(String.class));
        } 
        return new JSONObject(response);
    }

}
