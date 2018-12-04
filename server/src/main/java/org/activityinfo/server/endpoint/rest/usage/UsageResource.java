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
package org.activityinfo.server.endpoint.rest.usage;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.gson.stream.JsonWriter;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.DeploymentEnvironment;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.database.hibernate.entity.UserPermission;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.AbstractReturningWork;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Provides general usage statistics need for running the server.
 */
public class UsageResource {
    
    private Provider<EntityManager> entityManager;
    private String accessKey;

    public UsageResource(Provider<EntityManager> entityManager, DeploymentConfiguration deploymentConfiguration) {
        this.entityManager = entityManager;
        this.accessKey = deploymentConfiguration.getProperty("usage.access.key");
    }

    @GET
    @Path("accounts")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAccounts(@QueryParam("accessKey") String requestKey) {
        assertAuthorized(requestKey);
        return executeQuery("SELECT * from billingaccount");
    }

    @GET
    @Path("technicalContacts")
    @Produces(MediaType.APPLICATION_JSON)
    public String getTechnicalContacts(@QueryParam("accessKey") String requestKey) {
        assertAuthorized(requestKey);
        return executeQuery("SELECT userid, email, name, billingAccountId from userlogin where billingaccountid is not null");
    }

    @GET
    @Path("freeTrials")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFreeTrials(@QueryParam("accessKey") String requestKey) {
        assertAuthorized(requestKey);
        return executeQuery("SELECT userid, email, name, dateCreated, bounced, trialEndDate from userlogin where billingaccountid is null");
    }

    @GET
    @Path("databases")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDatabases(@QueryParam("accessKey") String requestKey) {
        assertAuthorized(requestKey);
        return executeQuery("SELECT databaseId, d.Name databaseName, d.fullName, d.lastSchemaUpdate, c.ISO2 as country," +
                "u.userId ownerUserId, u.email ownerEmail, u.name ownerName " +
                "FROM userdatabase d " + 
                "LEFT JOIN country c ON (d.countryId = c.countryId) " +
                "LEFT JOIN userlogin u ON (d.OwnerUserId=u.UserId) " +
                "WHERE dateDeleted IS NULL");
    }

    @GET
    @Path("databaseUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDatabaseUsers(@QueryParam("accessKey") String requestKey) {
        assertAuthorized(requestKey);
        return executeQuery("SELECT " +
                    "up.databaseId, up.partnerId, p.name partnerName," +
                    "u.email, u.name, " +
                    "up.allowDesign, up.allowEdit  " +
                "FROM userpermission up " +
                "LEFT JOIN userlogin u ON (u.UserId=up.UserId) " +
                "LEFT JOIN partner p ON (up.PartnerId=p.PartnerId) " +
                "WHERE AllowView=1 ");
    }

    @GET
    @Path("profile")
    @Produces(MediaType.APPLICATION_JSON)
    public String queryProfile(@QueryParam("accessKey") String requestKey, @QueryParam("user") String userQuery) {
        assertAuthorized(requestKey);


        User user;
        if(userQuery.matches("[0-9]+")) {
            user = entityManager.get().find(User.class, Integer.parseInt(userQuery));
        } else {
            user = entityManager.get().createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", userQuery)
                    .getSingleResult();
        }

        JsonValue profile = Json.createObject();
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("invited", user.getInvitedBy() != null);
        profile.put("dateCreated", user.getDateCreated().toString());

        if(user.getBillingAccount() != null) {
            JsonValue billingAccount = Json.createObject();
            billingAccount.put("name", user.getBillingAccount().getName());
            billingAccount.put("userLimit", user.getBillingAccount().getUserLimit());
            billingAccount.put("endTime", user.getBillingAccount().getEndTime().toString());
            profile.put("billingAccount", billingAccount);

        } else if(user.getTrialEndDate() != null) {
            profile.put("trialEndDate", user.getTrialEndDate().toString());
        }

        JsonValue databaseArray = Json.createArray();

        List<Database> ownedDatabases = entityManager.get().createQuery("select d from Database d where d.owner = :user and d.dateDeleted is null", Database.class)
                .setParameter("user", user)
                .getResultList();

        for (Database ownedDatabase : ownedDatabases) {
            JsonValue database = Json.createObject();
            database.put("role", "owns");
            database.put("name", ownedDatabase.getName());
            database.put("country", ownedDatabase.getCountry().getName());
            databaseArray.add(database);
        }

        List<UserPermission> permissions = entityManager.get().createQuery(
                "select p from UserPermission p inner join fetch p.database d where p.user = :user and p.allowView = 1",
                UserPermission.class)
                .setParameter("user", user)
                .getResultList();

        for (UserPermission permission : permissions) {
            if(!permission.getDatabase().isDeleted()) {
                JsonValue database = Json.createObject();
                database.put("name", permission.getDatabase().getName());
                database.put("country", permission.getDatabase().getCountry().getName());
                if (permission.isAllowDesign()) {
                    database.put("role", "designer");
                } else if (permission.isAllowEdit()) {
                    database.put("role", "editor");
                } else {
                    database.put("role", "viewer");
                }
                databaseArray.add(database);
            }
        }

        profile.put("databases", databaseArray);

        return profile.toJson();

    }

    private void assertAuthorized(@QueryParam("accessKey") String requestKey) {

        if(DeploymentEnvironment.isAppEngineDevelopment()) {
            return;
        }

        if(Strings.isNullOrEmpty(accessKey)) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
        }
        if(!accessKey.equals(requestKey)) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                    .entity("Invalid accessKey parameter").build());

        }
    }

    /**
     * Executes an SQL query and returns the results as a JSON object
     * @param sql
     * @return
     */
    private String executeQuery(final String sql) {

        HibernateEntityManager entityManager = (HibernateEntityManager) this.entityManager.get();
        return entityManager.getSession().doReturningWork(new AbstractReturningWork<String>() {
            @Override
            public String execute(Connection connection) throws SQLException {
                
                try(Statement statement = connection.createStatement()) {
                    try(ResultSet rs = statement.executeQuery(sql)) {

                        int numColumns = rs.getMetaData().getColumnCount();
                        String[] names = new String[numColumns];
                        int[] types = new int[numColumns];
                        for (int i = 0; i < numColumns; i++) {
                            names[i] = rs.getMetaData().getColumnLabel(i + 1);
                            types[i] = rs.getMetaData().getColumnType(i+1);
                        }

                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        
                        StringWriter json = new StringWriter();
                        JsonWriter jsonWriter = new JsonWriter(json);
                        jsonWriter.beginArray();

                        while(rs.next()) {
                            jsonWriter.beginObject();
                            for (int i = 0; i < numColumns; i++) {
                                jsonWriter.name(names[i]);
                                switch (types[i]) {
                                    case Types.BIGINT:
                                    case Types.SMALLINT:
                                    case Types.TINYINT:
                                    case Types.INTEGER:
                                    case Types.NUMERIC:
                                    case Types.FLOAT:
                                    case Types.DOUBLE:
                                    case Types.DECIMAL:
                                        jsonWriter.value(rs.getDouble(i+1));
                                        break;
                                    case Types.VARCHAR:
                                    case Types.NVARCHAR:
                                    case Types.CHAR:
                                    case Types.NCHAR:
                                    case Types.LONGNVARCHAR:
                                    case Types.LONGVARCHAR:
                                        jsonWriter.value(rs.getString(i+1));
                                        break;
                                    case Types.BIT:
                                    case Types.BOOLEAN:
                                        jsonWriter.value(rs.getBoolean(i+1));
                                        break;
                                    case Types.DATE:
                                    case Types.TIME:
                                    case Types.TIMESTAMP:
                                        Date date = rs.getDate(i + 1);
                                        if(date == null) {
                                            jsonWriter.nullValue();
                                        } else {
                                            jsonWriter.value(dateFormat.format(date));
                                        }
                                        break;
                                    default:
                                        throw new RuntimeException("Invalid type: " + types[i]);
                                }
                            }
                            jsonWriter.endObject();
                        }
                        jsonWriter.endArray();
                        jsonWriter.flush();
                        return json.toString();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }
    
}
