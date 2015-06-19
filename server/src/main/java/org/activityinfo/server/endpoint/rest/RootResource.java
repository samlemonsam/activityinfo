package org.activityinfo.server.endpoint.rest;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.legacy.shared.command.GetCountries;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.CountryDTO;
import org.activityinfo.legacy.shared.model.DTOViews;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.DeploymentEnvironment;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.HibernateQueryExecutor;
import org.activityinfo.server.database.hibernate.entity.AdminEntity;
import org.activityinfo.server.database.hibernate.entity.AdminLevel;
import org.activityinfo.server.database.hibernate.entity.Country;
import org.activityinfo.service.DeploymentConfiguration;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.mysql.collections.CountryCollection;
import org.activityinfo.store.query.impl.InvalidUpdateException;
import org.activityinfo.store.query.impl.Updater;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

@Path("/resources")
public class RootResource {

    private Provider<EntityManager> entityManager;
    private DispatcherSync dispatcher;
    private DeploymentConfiguration config;
    private HibernateQueryExecutor queryExecutor;
    private Provider<AuthenticatedUser> user;
    
    @Inject
    public RootResource(Provider<EntityManager> entityManager,
                        DispatcherSync dispatcher,
                        DeploymentConfiguration config, HibernateQueryExecutor queryExecutor) {
        super();
        this.entityManager = entityManager;
        this.dispatcher = dispatcher;
        this.config = config;
        this.queryExecutor = queryExecutor;
    }

    @Path("/adminEntity/{id}")
    public AdminEntityResource getAdminEntity(@PathParam("id") int id) {
        return new AdminEntityResource(entityManager.get().find(AdminEntity.class, id));
    }

    @GET 
    @Path("/countries")
    @JsonView(DTOViews.List.class)
    @Produces(MediaType.APPLICATION_JSON)
    public List<CountryDTO> getCountries() {

        QueryModel model = new QueryModel(CountryCollection.FORM_CLASS_ID);
        model.selectField(CountryCollection.CODE_FIELD_ID).as("code");
        model.selectField(CountryCollection.NAME_FIELD_ID).as("name");
        
        return dispatcher.execute(new GetCountries()).getData();
    }


    @Path("/country/{id: [0-9]+}")
    public CountryResource getCountryById(@PathParam("id") int id) {
        Country result = entityManager.get().find(Country.class, id);
        if (result == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        return new CountryResource(result);
    }

    @Path("/country/{code: [A-Z]+}")
    public CountryResource getCountryByCode(@PathParam("code") String code) {

        List<Country> results = entityManager.get()
                                             .createQuery("select c from Country c where c.codeISO = :iso")
                                             .setParameter("iso", code)
                                             .getResultList();

        if (results.isEmpty()) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        return new CountryResource(results.get(0));
    }

    @GET 
    @Path("/databases") 
    @JsonView(DTOViews.List.class) 
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserDatabaseDTO> getDatabases() {
        List<UserDatabaseDTO> databases = dispatcher.execute(new GetSchema()).getDatabases();
        return databases;
    }

    @Path("/database/{id}")
    public DatabaseResource getDatabaseSchema(@PathParam("id") int id) {
        return new DatabaseResource(dispatcher, id);
    }

    @Path("/adminLevel/{id}")
    public AdminLevelResource getAdminLevel(@PathParam("id") int id) {
        return new AdminLevelResource(queryExecutor, entityManager, entityManager.get().find(AdminLevel.class, id));
    }

    @Path("/sites")
    public SitesResources getSites() {
        return new SitesResources(dispatcher);
    }

    @Path("/tile")
    public TileResource getTile() {
        return new TileResource(config);
    }

    @Path("/locations")
    public LocationsResource getLocations() {
        return new LocationsResource(dispatcher);
    }

    @Path("/form/{id}")
    public FormResource getForm(@PathParam("id") ResourceId id) {
        return new FormResource(id, queryExecutor);
    }
    
    @Path("/query")
    public QueryResource query() {
        return new QueryResource(queryExecutor);
    }
    
    @POST
    @Path("/update") 
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(String json) {
        
        if(!DeploymentEnvironment.isAppEngineDevelopment() && !user.get().getEmail().endsWith("@bedatadriven.com")) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        Gson gson = new Gson();
        final JsonElement jsonElement = gson.fromJson(json, JsonElement.class);

        return queryExecutor.doWork(new HibernateQueryExecutor.StoreSession<Response>() {
            @Override
            public Response execute(CollectionCatalog catalog) {
                Updater updater = new Updater(catalog);
                try {
                    updater.execute(jsonElement.getAsJsonObject());
                } catch (InvalidUpdateException e) {
                    throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
                }
                return Response.ok().build();
            }
        });
    }
}
