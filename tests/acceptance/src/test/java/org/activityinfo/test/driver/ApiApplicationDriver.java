package org.activityinfo.test.driver;


import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.activityinfo.test.sut.Accounts;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

public class ApiApplicationDriver extends ApplicationDriver {

    private static final int RDC = 1;

    private final Server server;
    private final Accounts accounts;
    
    private final AliasTable aliases;
    
    private UserAccount currentUser = null;
    
    private List<Integer> createdDatabases = Lists.newArrayList();

    @Inject
    public ApiApplicationDriver(Server server, Accounts accounts, AliasTable aliases) {
        super(aliases);
        this.server = server;
        this.accounts = accounts;
        this.aliases = aliases;
    }

    private WebResource commandEndpoint() {
        return root().path("command");
    }

    private WebResource root() {
        Client client = new Client();
        if(currentUser != null) {
            client.addFilter(new HTTPBasicAuthFilter(currentUser.getEmail(), currentUser.getPassword()));
        }
        return client.resource(server.getRootUrl());
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
    public void createDatabase(TestObject map) throws Exception {

        JSONObject properties = new JSONObject();
        properties.put("name", map.getAlias());
        properties.put("countryId", 1);

        createEntity("UserDatabase", properties);
        
        createdDatabases.add(map.lookupId());
    }


    @Override
    public void createForm(TestObject form) throws Exception {
        JSONObject properties = new JSONObject();
        properties.put("name", form.getAlias());
        properties.put("databaseId", form.getId("database")); 
        properties.put("locationTypeId", form.getId("locationType", queryNullaryLocationType(RDC))); 
        
        createEntity("Activity", properties);
    }

    @Override
    public void createField(TestObject field) throws Exception {

        JSONObject properties = new JSONObject();
        properties.put("name", field.getAlias());
        properties.put("activityId", field.getId("form"));
        properties.put("type", field.getString("type"));
        properties.put("units", field.getString("units", "parsects"));

        createEntity("Indicator", properties);
    }

    @Override
    public void submitForm(String formName, List<FieldValue> values) throws Exception {
        int activityId = aliases.getId(formName);

        JSONObject properties = new JSONObject();
        properties.put("activityId", activityId);
        properties.put("locationId", queryNullaryLocationType(RDC));
        properties.put("id", aliases.generateId());
        properties.put("reportingPeriodId", aliases.generateId());
        properties.put("date1", "2014-01-01");
        properties.put("date2", "2014-02-01");
        
        for(FieldValue value : values) {
            switch (value.getField()) {
                case "partner":
                    properties.put("partnerId", aliases.getId(value.getValue()));
                    break;
                case "location":
                    properties.put("locationId", aliases.getId(value.getValue()));
                    break;
                case "fromDate":
                    properties.put("date1", value.getValue());
                    break;
                case "toDate":
                    properties.put("date2", value.getValue());
                    break;
                default:
                    int indicatorId = aliases.getId(value.getField());
                    properties.put("I" + indicatorId, value.maybeNumberValue());
                    break;
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
        partner.put("name", aliases.createAlias(partnerName));
        
        JSONObject command = new JSONObject();
        command.put("databaseId", databaseId);
        command.put("partner", partner);

        JSONObject response = executeCommand("AddPartner", command).get();
        
        int partnerId = response.getInt("newId");

        aliases.bindTestHandleToId(partnerName, partnerId);
    }

    @Override
    public void createProject(TestObject project) throws Exception {

        JSONObject properties = new JSONObject();
        properties.put("name", project.getAlias());

        JSONObject command = new JSONObject();
        command.put("databaseId", project.getId("database"));
        command.put("project", properties);
        
        JSONObject response = executeCommand("AddProject", command).get();
        
        int projectId = response.getInt("newId");
        
        aliases.bindTestHandleToId(project.getName(), projectId);
    }

    @Override
    public void grantPermission(TestObject properties) throws Exception {
        
        JSONObject model = new JSONObject();
        model.put("name", "A User");
        model.put("email", properties.getString("user"));
        model.put("partnerId", aliases.getId(properties.getString("partner")));
        
        for(String permission : properties.getStringList("permissions")) {
            switch (permission) {
                case "View":
                    model.put("allowView", true);
                    break;
                case "View All":
                    model.put("allowViewAll", true);
                    break;
                case "Edit":
                    model.put("allowEdit", true);
                    break;
                case "Edit All":
                    model.put("allowEditAll", true);
                    break;
                case "Manage Users":
                    model.put("allowManageUsers", true);
                    break;
                case "Manage All Users":
                    model.put("allowManageAllUsers", true);
                    break;
                case "Design":
                    model.put("allowDesign", true);
                    break;
            }
        }
        
        JSONObject command = new JSONObject();
        command.put("databaseId", aliases.getId(properties.getString("database")));
        command.put("model", model);
        
        executeCommand("UpdateUserPermissions", command);
    }

    @Override
    public void cleanup() throws Exception {
        for(Integer databaseId : createdDatabases) {
            executeDelete("UserDatabase", databaseId);
        }
    }

    @Override
    public void createLocationType(TestObject locationType) throws Exception {
        
        JSONObject properties = new JSONObject();
        properties.put("name", locationType.getAlias());
        properties.put("databaseId", locationType.getId("database"));
        
        createEntity("LocationType", properties);
    }

    @Override
    public void createLocation(TestObject map) throws Exception {
        
        JSONObject properties = new JSONObject();
        properties.put("id", aliases.generateIdFor(map.getName()));
        properties.put("name", map.getAlias());
        properties.put("axe", map.getString("code"));
        properties.put("locationTypeId", map.getId("locationType"));
        
        JSONObject command = new JSONObject();
        command.put("properties", properties);
        
        executeCommand("CreateLocation", command);
    }

    @Override
    public void createTarget(TestObject properties) throws Exception {

        JSONObject target = new JSONObject();
        target.put("name", properties.getAlias());
        target.put("fromDate", properties.getDate("fromDate", new LocalDate(1900,1,1)));
        target.put("toDate", properties.getDate("toDate", new LocalDate(2050,1,1)));

        JSONObject addTarget = new JSONObject();
        addTarget.put("databaseId", properties.getId("database"));
        addTarget.put("target", target);

        JSONObject response = executeCommand("AddTarget", addTarget).get();
        
        int id = response.getInt("newId");
        
        aliases.bindAliasToId(properties.getAlias(), id);
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

    private int createEntity(String entityType, JSONObject properties) throws JSONException {

        JSONObject object = new JSONObject();
        object.put("entityName", entityType);
        object.put("properties", properties);

        JSONObject response = executeCommand("CreateEntity", object).get();

        int newId = response.getInt("newId");
        aliases.bindAliasToId(properties.getString("name"), newId);
        
        return newId;
    }
    
    private int queryNullaryLocationType(int countryId) {
        String json = root().path("resources")
                .path("country").path(Integer.toString(countryId))
                .path("locationTypes")
                .get(String.class);

        try {
            JSONArray array = new JSONArray(json);

            for (int i = 0; i != array.length(); ++i) {
                JSONObject locationType = array.getJSONObject(i);
                if (locationType.getString("name").equals("Country")) {
                    return locationType.getInt("id");
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException("Exception parsing locationType list", e);
        }
        
        throw new IllegalStateException(String.format("Country %d has no nullary location type: expected" +
                " location type with name 'Country'", countryId));
    }

    private Optional<JSONObject> executeCommand(String type, JSONObject command) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("type", type);
        request.put("command", command);
        
        String json = request.toString();
        
        String response = null;
        try {
            response = commandEndpoint().type(MediaType.APPLICATION_JSON_TYPE).post(String.class, json);
            return Optional.of(new JSONObject(response));
            
        } catch (UniformInterfaceException e) {
            if(e.getResponse().getClientResponseStatus() == ClientResponse.Status.NO_CONTENT) {
                return Optional.absent();
            } else {
                throw new RuntimeException(e.getResponse().getStatus() + ": "
                        + e.getResponse().getEntity(String.class));
            }
        } 
    }
}
