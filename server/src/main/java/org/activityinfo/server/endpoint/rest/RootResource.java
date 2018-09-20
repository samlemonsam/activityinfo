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
package org.activityinfo.server.endpoint.rest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.GetCountries;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.CountryDTO;
import org.activityinfo.legacy.shared.model.DTOViews;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.command.handler.LegacyPermissionAdapter;
import org.activityinfo.server.database.hibernate.entity.AdminEntity;
import org.activityinfo.server.database.hibernate.entity.AdminLevel;
import org.activityinfo.server.database.hibernate.entity.Country;
import org.activityinfo.server.endpoint.rest.usage.UsageResource;
import org.activityinfo.server.mail.MailSender;
import org.activityinfo.store.mysql.collections.CountryTable;
import org.activityinfo.store.query.server.InvalidUpdateException;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.spi.DatabaseProvider;
import org.activityinfo.store.spi.FormStorageProvider;
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
    private Provider<FormStorageProvider> catalog;
    private Provider<AuthenticatedUser> userProvider;
    private ServerSideAuthProvider authProvider;
    private MailSender mailSender;
    private DatabaseProvider databaseProvider;

    private ApiBackend backend;
    
    @Inject
    public RootResource(Provider<EntityManager> entityManager,
                        Provider<FormStorageProvider> catalog,
                        DispatcherSync dispatcher,
                        DeploymentConfiguration config,
                        Provider<AuthenticatedUser> userProvider,
                        ServerSideAuthProvider authProvider,
                        ApiBackend backend,
                        MailSender mailSender,
                        DatabaseProvider databaseProvider) {
        super();
        this.entityManager = entityManager;
        this.dispatcher = dispatcher;
        this.catalog = catalog;
        this.config = config;
        this.userProvider = userProvider;
        this.authProvider = authProvider;
        this.backend = backend;
        this.mailSender = mailSender;
        this.databaseProvider = databaseProvider;
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

        QueryModel model = new QueryModel(CountryTable.FORM_CLASS_ID);
        model.selectField(CountryTable.CODE_FIELD_ID).as("code");
        model.selectField(CountryTable.NAME_FIELD_ID).as("name");
        
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
        return dispatcher.execute(new GetSchema()).getDatabases();
    }

    @Path("/database/{id}")
    public DatabaseResource getDatabaseSchema(@PathParam("id") int id) {
        return new DatabaseResource(catalog,
                dispatcher,
                new DatabaseProviderImpl(entityManager),
                entityManager,
                mailSender,
                id);
    }

    @Path("/adminLevel/{id}")
    public AdminLevelResource getAdminLevel(@PathParam("id") int id) {
        return new AdminLevelResource(
                backend, entityManager.get().find(AdminLevel.class, id));
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
        return new FormResource(backend, id);
    }
    
    @Path("/query")
    public QueryResource query() {
        return new QueryResource(backend);
    }
    
    @POST
    @Path("/update") 
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(String json) {

        final JsonValue jsonElement = Json.parse(json);

        Updater updater = backend.newUpdater();
        try {
            updater.execute(jsonElement);
        } catch (InvalidUpdateException e) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build());
        }
        return Response.ok().build();
    }

    @Path("/users")
    public UsersResource getUsers() {
        return new UsersResource(config, entityManager);
    }


    @Path("/usage")
    public UsageResource getUsage() {
        return new UsageResource(entityManager, config);
    }
    
    @Path("/testPivot")
    public PivotTestResource getPivotTestResource() {
        return new PivotTestResource( dispatcher, authProvider);
    }
    
    @Path("/catalog")
    public CatalogResource getFormCatalog(@QueryParam("parent") String parentId) {
        return new CatalogResource(backend);
    }

    @Path("/analysis")
    public AnalysesResource getAnalysis() {
        return new AnalysesResource(databaseProvider, userProvider);
    }

    @Path("/accounts")
    public AccountResource getAccounts() {
        return new AccountResource(entityManager.get(), userProvider);
    }
}
