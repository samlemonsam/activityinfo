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

import com.google.inject.Provider;
import org.activityinfo.io.xform.XFormReader;
import org.activityinfo.io.xform.form.XForm;
import org.activityinfo.legacy.shared.command.CreateEntity;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.store.spi.FormStorageProvider;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

public class DatabaseResource {

    private Provider<FormStorageProvider> catalog;
    private final DispatcherSync dispatcher;
    private final int databaseId;

    public DatabaseResource(Provider<FormStorageProvider> catalog, DispatcherSync dispatcher, int databaseId) {
        this.catalog = catalog;
        this.dispatcher = dispatcher;
        this.databaseId = databaseId;
    }

    private UserDatabaseDTOWithForms getSchema() {
        UserDatabaseDTO db = dispatcher.execute(new GetSchema()).getDatabaseById(databaseId);
        if (db == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        UserDatabaseDTOWithForms dbWithForms = new UserDatabaseDTOWithForms(db);
        for (ActivityDTO activity : db.getActivities()) {
            dbWithForms.getActivities().add(dispatcher.execute(new GetActivityForm(activity.getId())));
        }
        return dbWithForms;
    }

    @GET
    @Path("schema")
    @JsonView(DTOViews.Schema.class)
    @Produces(MediaType.APPLICATION_JSON)
    public UserDatabaseDTOWithForms getDatabaseSchema() {
        return getSchema();
    }


    @GET
    @Path("schema.csv")
    public Response getDatabaseSchemaCsv() throws IOException {
        SchemaCsvWriter writer = new SchemaCsvWriter(dispatcher);
        writer.write(databaseId);

        return writeCsv("schema_" + databaseId + ".csv", writer.toString());
    }


    @GET
    @Path("schema-v3.csv")
    public Response getDatabaseSchemaV3() throws IOException {

        UserDatabaseDTO db = dispatcher.execute(new GetSchema()).getDatabaseById(databaseId);

        SchemaCsvWriterV3 writer = new SchemaCsvWriterV3(catalog.get());
        writer.writeForms(db);

        return writeCsv("schema-v3_" + databaseId + ".csv", writer.toString());
    }

    private Response writeCsv(String filename, String text) {
        return Response.ok()
                .type("text/csv;charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=" + filename)
                .entity(text)
                .build();
    }
    


    @POST
    @Path("/forms")
    @Consumes("application/xml")
    public Response createFormFromXForm(@Context UriInfo uri, XForm xForm) {

        UserDatabaseDTOWithForms schema = getSchema();
        LocationTypeDTO locationType = schema.getCountry().getNullLocationType();

        ActivityFormDTO activityDTO = new ActivityFormDTO();
        activityDTO.setName(xForm.getHead().getTitle());
        activityDTO.set("databaseId", databaseId);
        activityDTO.set("locationTypeId", locationType.getId());

        CreateResult createResult = dispatcher.execute(new CreateEntity(activityDTO));
        int activityId = createResult.getNewId();

        XFormReader builder = new XFormReader(xForm);
        FormClass formClass = builder.build();
        formClass.setId(CuidAdapter.activityFormClass(activityId));
        formClass.setDatabaseId(CuidAdapter.databaseId(databaseId));

        MySqlStorageProvider formCatalog = (MySqlStorageProvider) catalog.get();
        formCatalog.createOrUpdateFormSchema(formClass);
        
        return Response.created(uri.getAbsolutePathBuilder()
                .path(RootResource.class).path("forms").path(formClass.getId().asString())
                .build())
                .build();
    }
}
