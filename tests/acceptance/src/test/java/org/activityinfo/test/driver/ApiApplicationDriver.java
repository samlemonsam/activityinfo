package org.activityinfo.test.driver;


import com.google.common.collect.Lists;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.activityinfo.test.sut.*;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class ApiApplicationDriver implements ApplicationDriver {

    private final Server server;
    private final Accounts accounts;
    
    private final AliasTable aliases;
    
    private UserAccount currentUser = null;
    
    private List<Integer> createdDatabases = Lists.newArrayList();

    @Inject
    public ApiApplicationDriver(Server server, Accounts accounts, AliasTable aliases) {
        this.server = server;
        this.accounts = accounts;
        this.aliases = aliases;
    }

    private WebResource commandEndpoint() {
        Client client = new Client();
        if(currentUser != null) {
            client.addFilter(new HTTPBasicAuthFilter(currentUser.getEmail(), currentUser.getPassword()));
        }
        return client.resource(server.getRootUrl()).path("command");
    }

    @Override
    public void login() {
        currentUser = accounts.any();
    }

    @Override
    public void login(UserAccount account) {
        currentUser = account;
    }

    @Override
    public ApplicationDriver setup() {
        return this;
    }

    @Override
    public void createDatabase(Property... databaseProperties) throws Exception {
        TestObject map = new TestObject(databaseProperties);

        String name = map.getString("name");
        JSONObject properties = new JSONObject();
        properties.put("name", aliases.create(name));
        properties.put("countryId", 1);

        createEntity("UserDatabase", name, properties);
        
        createdDatabases.add(aliases.getId(name));
    }


    @Override
    public void createForm(Property... arguments) throws Exception {
        TestObject map = new TestObject(arguments);
        
        String name = map.getString("name");
        JSONObject properties = new JSONObject();
        properties.put("name", aliases.create(name));
        properties.put("databaseId", aliases.getId(map.getString("database")));
        properties.put("locationTypeId", 50529); // TODO: de-hardcode (this is Rdc/Country)
        createEntity("Activity", name, properties);
    }

    @Override
    public void createField(Property... arguments) throws Exception {
        TestObject map = new TestObject(arguments);

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
        properties.put("reportingPeriodId", aliases.generateId());
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
    public void delete(String objectType, String name) throws Exception {
        
        switch(objectType) {
            case "database":
                executeDelete("UserDatabase", aliases.getId(name));
                break;
            default:
                throw new IllegalArgumentException(String.format("Invalid object type '%s'", objectType));
        }
    }

    private void executeDelete(String objectType, int objectId) throws Exception {
        JSONObject command = new JSONObject();
        command.put("entityName", objectType);
        command.put("id", objectId);

        executeCommand("Delete", command);
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
    public void createProject(Property... arguments) throws Exception {
        TestObject properties = new TestObject(arguments);

        int databaseId = aliases.getId(properties.getString("database"));
        String alias = properties.getString("name");
        
        JSONObject project = new JSONObject();
        project.put("name", aliases.create(alias));

        JSONObject command = new JSONObject();
        command.put("databaseId", databaseId);
        command.put("project", project);
        
        JSONObject response = executeCommand("AddProject", command);
        
        int projectId = response.getInt("newId");
        
        aliases.mapId(alias, projectId);
    }

    @Override
    public void pivotTable(String measure, String rowDimension) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanup() throws Exception {
        for(Integer databaseId : createdDatabases) {
            executeDelete("UserDatabase", databaseId);
        }
    }

    @Override
    public void createTarget(Property... arguments) throws Exception {
        TestObject properties = new TestObject(arguments);

        String name = aliases.create(properties.getString("name"));
        
        JSONObject target = new JSONObject();
        target.put("name", name);
        target.put("date1", properties.getDate("date1", new LocalDate(1900,1,1)));
        target.put("date2", properties.getDate("date2", new LocalDate(2050,1,1)));

        JSONObject addTarget = new JSONObject();
        addTarget.put("databaseId", aliases.getId(properties.getString("database")));
        addTarget.put("target", target);

        JSONObject response = executeCommand("AddTarget", addTarget);
        
        int id = response.getInt("newId");
        aliases.mapNameToId(name, id);
    }

    @Override
    public void setTargetValues(String targetName, List<FieldValue> values) throws Exception {
  
        for(FieldValue value : values) {

            JSONObject changes = new JSONObject();
            changes.put("value", value.asDouble());
            
            JSONObject command = new JSONObject();
            command.put("targetId", aliases.getId(targetName));
            command.put("indicatorId", aliases.getId(value.getField()));
            command.put("changes", changes);
            
            executeCommand("UpdateTargetValue", command);
        }
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
        
        String json = request.toString();
        
        String response = null;
        try {
            response = commandEndpoint().type(MediaType.APPLICATION_JSON_TYPE).post(String.class, json);
        } catch (UniformInterfaceException e) {
            throw new RuntimeException("400: " + e.getResponse().getEntity(String.class));
        } 
        return new JSONObject(response);
    }
}
