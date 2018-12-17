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
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.io.xform.XFormReader;
import org.activityinfo.io.xform.form.XForm;
import org.activityinfo.json.JsonParser;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.CreateEntity;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.database.transfer.RequestTransfer;
import org.activityinfo.model.database.transfer.TransferAuthorized;
import org.activityinfo.model.database.transfer.TransferDecision;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.authentication.AuthTokenProvider;
import org.activityinfo.server.authentication.SecureTokenGenerator;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.Partner;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.database.hibernate.entity.UserPermission;
import org.activityinfo.server.mail.*;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.store.spi.DatabaseProvider;
import org.activityinfo.store.query.UsageTracker;
import org.activityinfo.store.spi.FormStorageProvider;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.*;

public class DatabaseResource {

    private Provider<FormStorageProvider> catalog;
    private final DispatcherSync dispatcher;
    private final DatabaseProvider databaseProvider;
    private final Provider<EntityManager> entityManagerProvider;
    private final MailSender mailSender;
    private final ResourceId databaseId;
    private static final JsonParser PARSER = new JsonParser();

    public DatabaseResource(Provider<FormStorageProvider> catalog,
                            DispatcherSync dispatcher,
                            DatabaseProvider databaseProvider,
                            Provider<EntityManager> entityManagerProvider,
                            MailSender mailSender,
                            ResourceId databaseId) {
        this.catalog = catalog;
        this.dispatcher = dispatcher;
        this.databaseProvider = databaseProvider;
        this.entityManagerProvider = entityManagerProvider;
        this.mailSender = mailSender;
        this.databaseId = databaseId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UserDatabaseMeta getDatabaseMetadata(@InjectParam AuthenticatedUser user) {
        Optional<UserDatabaseMeta> db = databaseProvider.getDatabaseMetadata(databaseId, user.getUserId());
        if (!db.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return db.get();
    }

    private UserDatabaseDTOWithForms getSchema() {
        UserDatabaseDTO db = dispatcher.execute(new GetSchema()).getDatabaseById(CuidAdapter.getLegacyIdFromCuid(databaseId));
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
        writer.write(CuidAdapter.getLegacyIdFromCuid(databaseId));

        return writeCsv("schema_" + databaseId + ".csv", writer.toString());
    }


    @GET
    @Path("schema-v3.csv")
    public Response getDatabaseSchemaV3() throws IOException {

        UserDatabaseDTO db = dispatcher.execute(new GetSchema()).getDatabaseById(CuidAdapter.getLegacyIdFromCuid(databaseId));

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
        formClass.setDatabaseId(databaseId);

        MySqlStorageProvider formCatalog = (MySqlStorageProvider) catalog.get();
        formCatalog.createOrUpdateFormSchema(formClass);
        
        return Response.created(uri.getAbsolutePathBuilder()
                .path(RootResource.class).path("forms").path(formClass.getId().asString())
                .build())
                .build();
    }

    private Database getDatabase() {
        return entityManagerProvider.get().find(Database.class, CuidAdapter.getLegacyIdFromCuid(databaseId));
    }

    private Optional<User> getUserByEmail(String userEmail) {
        User user;
        try {
            user = entityManagerProvider.get().createQuery(
                    "SELECT u FROM User u " +
                            "WHERE u.email = :email", User.class)
                    .setParameter("email", userEmail)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @POST
    @Path("/transfer/request")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestTransfer(@InjectParam AuthenticatedUser executingUser, RequestTransfer request) {
        Database database = getDatabase();
        User currentOwner = database.getOwner();
        Optional<User> proposedOwner = getUserByEmail(request.getProposedOwnerEmail());

        if (executingUser.isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (executingUser.getUserId() != currentOwner.getId()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        if (database.getTransferToken() != null) {
            return Response.status(Response.Status.CONFLICT).entity("There is a pending transfer on this database").build();
        }
        if (!proposedOwner.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("The email '" + request.getProposedOwnerEmail() + "' is not registered on ActivityInfo.org.").build();
        }
        if (currentOwner.equals(proposedOwner.get())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User already owns database.").build();
        }

        startTransaction();
        try {
            generateTransferToken(database, proposedOwner.get());
            sendRequestNotifications(currentOwner, proposedOwner.get(), database);
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException(e);
        }
        commitTransaction();

        UsageTracker.track(currentOwner.getId(), "db_transfer_request", database.getResourceId());

        return Response.ok().entity("Request for transfer of database " + database.getName() + " has been sent.").build();
    }

    private void sendRequestNotifications(User currentOwner, User proposedOwner, Database database) {
        mailSender.send(new RequestDatabaseTransferMessage(proposedOwner, currentOwner, database));
    }

    private String generateTransferToken(Database database, User proposedOwner) {
        database.setTransferToken(SecureTokenGenerator.generate());
        database.setTransferUser(proposedOwner);
        database.setTransferRequestDate(new Date());
        entityManagerProvider.get().persist(database);
        return database.getTransferToken();
    }

    public Response startTransfer(UriInfo uri, AuthTokenProvider authTokenProvider, TransferAuthorized transfer) {
        Database database = getDatabase();
        User currentOwner = database.getOwner();
        User newOwner = database.getTransferUser();

        if (database.getTransferToken() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Database " + databaseId + " has no pending transfers").build();
        }
        if (!database.getTransferToken().equals(transfer.getToken())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Incorrect Token: " + transfer.getToken()).build();
        }
        if (!matchingRequest(transfer, currentOwner, newOwner)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(transfer.toJson().toJson()).build();
        }

        startTransaction();
        try {
            removeUserPermissions(database, newOwner);
            transferDatabase(database, newOwner);
            clearTransferToken(database);
            addUserPermissions(database, currentOwner);
            sendSuccessNotifications(currentOwner, newOwner, database);
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException(e);
        }
        commitTransaction();

        UsageTracker.track(newOwner.getId(), "db_transfer_complete", database.getResourceId());

        return Response.seeOther(uri.getAbsolutePathBuilder().replacePath("/app").fragment("db/" + databaseId).build())
                .cookie(authTokenProvider.createNewAuthCookies(newOwner))
                .build();
    }

    private void rollbackTransaction() {
        entityManagerProvider.get().getTransaction().rollback();
    }

    private void commitTransaction() {
        entityManagerProvider.get().getTransaction().commit();
    }

    private void startTransaction() {
        if (!entityManagerProvider.get().getTransaction().isActive()) {
            entityManagerProvider.get().getTransaction().begin();
        }
    }

    private void addUserPermissions(Database database, User currentOwner) {
        Partner defaultPartner = getDefaultPartner(database);
        UserPermission userPermission = new UserPermission(database, currentOwner);
        userPermission.setAllowView(true);
        userPermission.setAllowViewAll(false);
        userPermission.setAllowEdit(false);
        userPermission.setAllowEditAll(false);
        userPermission.setAllowDesign(false);
        userPermission.setAllowManageUsers(false);
        userPermission.setAllowManageAllUsers(false);
        userPermission.setAllowExport(false);
        userPermission.setAllowExportAll(false);
        userPermission.setAllowImport(false);
        userPermission.setAllowImportAll(false);
        userPermission.setPartner(defaultPartner);
        entityManagerProvider.get().persist(userPermission);
    }

    private Partner getDefaultPartner(Database database) {
        Partner defaultPartner;
        try {
            defaultPartner = entityManagerProvider.get().createQuery(
                    "SELECT p FROM Partner p " +
                            "LEFT JOIN FETCH p.databases " +
                            "WHERE p.name = :defaultName " +
                            "AND :database MEMBER OF p.databases", Partner.class)
                    .setParameter("defaultName", PartnerDTO.DEFAULT_PARTNER_NAME)
                    .setParameter("database", database)
                    .getSingleResult();
            return defaultPartner;
        } catch (NoResultException e) {
            defaultPartner = new Partner();
            defaultPartner.setName(PartnerDTO.DEFAULT_PARTNER_NAME);
            defaultPartner.setDatabases(Collections.singleton(database));
            entityManagerProvider.get().persist(defaultPartner);
            return defaultPartner;
        }
    }

    private void removeUserPermissions(Database database, User newOwner) {
        UserPermission userPermission = getUserPermission(database, newOwner);
        if (userPermission == null) {
            return;
        }
        entityManagerProvider.get().remove(userPermission);
    }

    private UserPermission getUserPermission(Database database, User user) {
        try {
            UserPermission userPermission = entityManagerProvider.get().createQuery(
                    "SELECT up FROM UserPermission up " +
                            "WHERE up.user.id = :userId AND up.database.id = :databaseId", UserPermission.class)
                    .setParameter("userId", user.getId())
                    .setParameter("databaseId", database.getId())
                    .getSingleResult();
            return userPermission;
        } catch (NoResultException e) {
            return null;
        }
    }

    private boolean matchingRequest(TransferAuthorized transfer, User currentOwner, User newOwner) {
        if (transfer.getCurrentOwner() != currentOwner.getId()) {
            return false;
        } else if (transfer.getProposedOwner() != newOwner.getId()) {
            return false;
        } else if (!CuidAdapter.databaseId(transfer.getDatabase()).equals(databaseId)) {
            return false;
        } else {
            return true;
        }
    }

    private void clearTransferToken(Database database) {
        database.setTransferToken(null);
        database.setTransferUser(null);
        database.setTransferRequestDate(null);
        entityManagerProvider.get().persist(database);
    }

    private void sendSuccessNotifications(User currentOwner, User newOwner, Database database) {
        mailSender.send(new SuccessfulDatabaseTransferMessage(currentOwner, database));
        mailSender.send(new SuccessfulDatabaseTransferMessage(newOwner, database));
    }

    private void transferDatabase(Database database, User newOwner) {
        database.setOwner(newOwner);
        entityManagerProvider.get().persist(database);
    }

    @POST
    @Path("/transfer/cancel")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cancelTransfer(@InjectParam AuthenticatedUser executingUser, TransferDecision decision) {
        Database database = getDatabase();
        User currentOwner = database.getOwner();
        User proposedOwner = database.getTransferUser();

        if (executingUser.isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (executingUser.getUserId() != currentOwner.getId() && executingUser.getUserId() != proposedOwner.getId()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        if (database.getTransferToken() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Database " + databaseId + " has no pending transfers").build();
        }

        startTransaction();
        try {
            clearTransferToken(database);
            if (decision.isRejected()) {
                sendRejectionNotifications(currentOwner, proposedOwner, database);
            } else {
                sendCancelledNotifications(currentOwner, proposedOwner, database);
            }
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException(e);
        }
        commitTransaction();

        UsageTracker.track(executingUser.getId(), "db_transfer_cancel", database.getResourceId());

        return Response.ok().entity("Database transfer cancelled").build();
    }

    private void sendCancelledNotifications(User currentOwner, User proposedOwner, Database database) {
        mailSender.send(new CancelDatabaseTransferOwnerMessage(currentOwner, proposedOwner, database));
        mailSender.send(new CancelDatabaseTransferApproverMessage(proposedOwner, proposedOwner, database));
    }

    private void sendRejectionNotifications(User currentOwner, User proposedOwner, Database database) {
        mailSender.send(new RejectDatabaseTransferOwnerMessage(currentOwner, proposedOwner, database));
        mailSender.send(new RejectDatabaseTransferApproverMessage(proposedOwner, proposedOwner, database));
    }

}
