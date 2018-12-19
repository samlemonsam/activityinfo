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
package org.activityinfo.geoadmin.model;

import com.bedatadriven.geojson.GeoJsonModule;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBWriter;
import org.activityinfo.client.ActivityInfoClient;
import org.activityinfo.geoadmin.source.FeatureSourceStorageProvider;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.api.ClientVersions;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.JsonFormTreeBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransactionBuilder;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ContextResolver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * ActivityInfo REST Client
 */
public class GeoAdminClient implements FormClassProvider {

    public static final Logger LOGGER = Logger.getLogger(GeoAdminClient.class.getName());

    private Client client;
    private URI root;

    private ActivityInfoClient remote;
    
    private FeatureSourceStorageProvider localCatalog = new FeatureSourceStorageProvider();

    @Override
    public FormClass getFormClass(ResourceId formId) {
        Preconditions.checkArgument(formId.getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN);
        return GeoAdminClient.this.getFormClass(CuidAdapter.getLegacyIdFromCuid(formId));
    }

    public static class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

        @Override
        public ObjectMapper getContext(Class<?> type) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new GeoJsonModule());
            return mapper;
        }

    }

    /**
     * Creates a new instance using the given endpoint, ActivityInfo username
     * and password.
     * 
     * @param endpoint
     *            Rest endpoint (for example:
     *            https://www.activityinfo.org/resources)
     * @param username
     *            Email address of user (for example: akbertram@gmail.com)
     * @param password
     *            User's plaintext password
     */
    public GeoAdminClient(String endpoint, String username, String password) {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        clientConfig.getClasses().add(ObjectMapperProvider.class);
        client = Client.create(clientConfig);
        client.addFilter(new HTTPBasicAuthFilter(username, password));
        client.addFilter(new ClientFilter() {
            @Override
            public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
                cr.getHeaders().add(ClientVersions.CLIENT_VERSION_HEADER, ClientVersions.CLIENT_VERSION);
                return getNext().handle(cr);
            }
        });

        root = UriBuilder.fromUri(endpoint).build();

        remote = new ActivityInfoClient(endpoint, username, password);
    }



    /**
     * @return the list of Countries in ActivityInfo's geographic reference
     *         database
     */
    public List<Country> getCountries() {
        URI uri = UriBuilder.fromUri(root).path("countries").build();
        return Arrays.asList(
            client.resource(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Country[].class));
    }

    /**
     * @return the list of Administrative levels for a given country in
     *         ActivityInfo's geographic reference database
     */
    public List<AdminLevel> getAdminLevels(Country country) {
        return getAdminLevelsByCountryCode(country.getCode());
    }

	public List<AdminLevel> getAdminLevelsByCountryCode(String countryCode) {
		URI uri = UriBuilder.fromUri(root)
            .path("country")
            .path(countryCode)
            .path("adminLevels").build();
        return Arrays.asList(
                client.resource(uri)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(AdminLevel[].class));
	}
	
	public List<LocationType> getLocationTypesByCountryCode(String countryCode) {
		URI uri = UriBuilder.fromUri(root)
	            .path("country")
	            .path(countryCode)
	            .path("locationTypes").build();
	        return Arrays.asList(
	            client.resource(uri)
	                .accept(MediaType.APPLICATION_JSON_TYPE)
	                .get(LocationType[].class));	
	}
	
	public List<Location> getLocations(int locationType) {
		URI uri = UriBuilder.fromUri(root)
	            .path("locations")
	            .queryParam("type", locationType)
	            .build();
        return Arrays.asList(
            client.resource(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Location[].class));	
	}
	
	public void postNewLocations(int locationType, List<NewLocation> locations) {
		URI uri = UriBuilder.fromUri(root)
	            .path("locations")
	            .path(Integer.toString(locationType))
	            .build();
	
        client.resource(uri)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .post(locations);	
		
	}
	
    public void updateAdminLevel(AdminLevel level) {
        URI uri = UriBuilder.fromUri(root)
            .path("adminLevel")
            .path(Integer.toString(level.getId()))
            .build();

        client.resource(uri)
            .accept(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_JSON)
            .put(level);
    }
    
    public void deleteLevel(AdminLevel level) {
        URI uri = UriBuilder.fromUri(root)
                .path("adminLevel")
                .path(Integer.toString(level.getId()))
                .build();

        client.resource(uri)
        	.delete();
    }

    public AdminLevel getAdminLevel(int id) {
        URI build = UriBuilder.fromUri(root)
            .path("adminLevel")
            .path(Integer.toString(id))
            .build();
        return client.resource(build)
            .accept(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_JSON)
            .get(AdminLevel.class);
    }

    public List<AdminEntity> getAdminEntities(AdminLevel level) {
        return getAdminEntities(level.getId());
    }

    public List<AdminEntity> getAdminEntities(int levelId) {

        LOGGER.info("Fetching admin entities for level " + levelId);

        URI uri = UriBuilder.fromUri(root)
            .path("adminLevel")
            .path(Integer.toString(levelId))
            .path("entities")
            .build();

        return Arrays.asList(
                client.resource(uri)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(AdminEntity[].class));
    }

    /**
     * Creates a new root administrative level for a given country
     */
    public void postRootLevel(Country country, AdminLevel newLevel) {
        URI uri = UriBuilder.fromUri(root)
            .path("country")
            .path(country.getCode())
            .path("adminLevels")
            .build();
        client.resource(uri)
            .accept(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_JSON)
            .post(newLevel);
    }

    /**
     * Creates a new root administrative level for a given country
     */
    public void postChildLevel(AdminLevel parentLevel, AdminLevel newLevel) {
        URI uri = UriBuilder.fromUri(root)
            .path("adminLevel")
            .path(Integer.toString(parentLevel.getId()))
            .path("childLevels")
            .build();
        client.resource(uri)
            .accept(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_JSON)
            .post(newLevel);
    }
    
    public List<AdminEntity> geocode(double latitude, double longitude) {
	   URI uri = UriBuilder.fromUri(root)
	            .path("geocode")
	            .queryParam("lat", latitude)
	            .queryParam("lng", longitude)
	            .build();

       return Arrays.asList(
           client.resource(uri)
               .accept(MediaType.APPLICATION_JSON_TYPE)
               .get(AdminEntity[].class));    
    }
    
    public List<List<AdminEntity>> geocode(List<Point> points) {
    	List<LatLng> latLngs = Lists.newArrayList();
    	for(Point point : points) {
    		latLngs.add(new LatLng(point));
    	}
    	return geocodePoints(latLngs);
    }
    
    public List<List<AdminEntity>> geocodePoints(List<LatLng> points) {
    	 URI uri = UriBuilder.fromUri(root)
 	            .path("geocode")
 	            .build();
    	 
        return client.resource(uri)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .post(new GenericType<List<List<AdminEntity>>>() {
            }, points);
    }


    public FormClass getFormClass(int adminLevelId) {
        
        String json = formResource(CuidAdapter.adminLevelFormClass(adminLevelId)).path("class").get(String.class);
        return FormClass.fromJson(json);
    }

    public FormTree getFormTree(ResourceId resourceId) {
        
        if(localCatalog.isLocalResource(resourceId)) {
            FormTreeBuilder treeBuilder = new FormTreeBuilder(localCatalog);
            return treeBuilder.queryTree(resourceId);
        
        } else {

            String json = formResource(resourceId).path("tree").get(String.class);
            JsonValue object = Json.parse(json);
            return JsonFormTreeBuilder.fromJson(object);
        }
    }

    public FormTree getFormTree(int adminLevelId) {
        return getFormTree(CuidAdapter.adminLevelFormClass(adminLevelId));
    }
    
    public ColumnSet queryColumns(QueryModel queryModel) {
        
        if(localCatalog.isLocalQuery(queryModel)) {
            ColumnSetBuilder builder = new ColumnSetBuilder(localCatalog, new NullFormSupervisor());
            return builder.build(queryModel);

        } else {
            return remote.queryTable(queryModel);
        }
    }

    public void updateGeometry(ResourceId formId, ResourceId recordId, ResourceId fieldId, Geometry value) {
        ClientResponse response = client.resource(root)
                .path("form")
                .path(formId.asString())
                .path("record")
                .path(recordId.asString())
                .path("field")
                .path(fieldId.asString())
                .path("geometry")
                .entity(toWkbBinary(value))
                .post(ClientResponse.class);

        if(response.getStatus() != 200) {
            throw new RuntimeException("Failed with status code: " + response.getStatus() + ", message: " +
                firstLine(response.getEntity(String.class)));
        }
    }

    private String firstLine(String response) {
        int endOfFirstLine = response.indexOf('\n');
        if(endOfFirstLine == -1) {
            return response;
        } else {
            return response.substring(0, endOfFirstLine);
        }
    }

    private Object toWkbBinary(Geometry value) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WKBWriter writer = new WKBWriter();
        try {
            writer.write(value, new OutputStreamOutStream(baos));
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode geometry as WKB", e);
        }
        return baos.toByteArray();
    }

    public void executeTransaction(RecordTransactionBuilder builder) {
        ClientResponse response = client.resource(root)
                .path("update")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(builder.toJsonObject().toJson(), MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class);
        
        if(response.getStatus() != 200) {
            throw new RuntimeException("Transaction failed with status code: " + response.getStatus() + " "  +
            response.getEntity(String.class));
        }
    }
    
    private WebResource formResource(ResourceId resourceId) {
        return client.resource(root)
                .path("form")
                .path(resourceId.asString());
    }
}
