package org.activityinfo.server.endpoint.rest.usage;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.gson.stream.JsonWriter;
import org.activityinfo.server.DeploymentEnvironment;
import org.activityinfo.service.DeploymentConfiguration;
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
                                        throw new RuntimeException("Invalid type: " + i);
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
