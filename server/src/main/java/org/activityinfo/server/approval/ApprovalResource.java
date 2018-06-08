package org.activityinfo.server.approval;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.database.transfer.TransferAuthorized;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.endpoint.rest.DatabaseProviderImpl;
import org.activityinfo.server.endpoint.rest.DatabaseResource;
import org.activityinfo.server.mail.MailSender;
import org.activityinfo.store.spi.FormStorageProvider;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * REST Endpoint for user confirmations. Currently only implemented for Database Transfer requests.
 */
@Path("/approval")
public class ApprovalResource {

    private static final Logger LOGGER = Logger.getLogger(ApprovalResource.class.getName());

    private Provider<EntityManager> entityManager;
    private DispatcherSync dispatcher;
    private Provider<FormStorageProvider> catalog;
    private MailSender mailSender;

    @Inject
    public ApprovalResource(Provider<EntityManager> entityManager,
                            DispatcherSync dispatcher,
                            Provider<FormStorageProvider> catalog,
                            MailSender mailSender) {
        this.entityManager = entityManager;
        this.dispatcher = dispatcher;
        this.catalog = catalog;
        this.mailSender = mailSender;
    }

    @GET
    @Path("/accept")
    public Response accept(@InjectParam AuthenticatedUser user, @QueryParam("token") String token) {
        Database database;
        try {
            database = entityManager.get().createQuery("SELECT db FROM Database db " +
                    "WHERE db.transferToken = :token", Database.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch(NoResultException noResult) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Token is no longer valid. Database transfer may have been cancelled, or may already have been approved").build();
        }

        User currentOwner = database.getOwner();
        User proposedOwner = database.getTransferUser();

        if (user.isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (proposedOwner.getId() != user.getUserId()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        TransferAuthorized transfer = new TransferAuthorized(currentOwner.getId(), proposedOwner.getId(), database.getId(), token);

        DatabaseResource resource = new DatabaseResource(catalog,
                dispatcher,
                new DatabaseProviderImpl(entityManager),
                entityManager,
                mailSender,
                database.getId());

        return resource.startTransfer(user, transfer);
    }

    @GET
    @Path("/reject")
    public Response reject(@InjectParam AuthenticatedUser user, @QueryParam("token") String token) {
        Database database;
        try {
            database = entityManager.get().createQuery("SELECT db FROM Database db " +
                    "WHERE db.transferToken = :token", Database.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch(NoResultException noResult) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Token is no longer valid. Database transfer may have been cancelled, or may already have been approved").build();
        }

        User proposedOwner = database.getTransferUser();

        if (user.isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (proposedOwner.getId() != user.getUserId()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        DatabaseResource resource = new DatabaseResource(catalog,
                dispatcher,
                new DatabaseProviderImpl(entityManager),
                entityManager,
                mailSender,
                database.getId());

        return resource.cancelTransfer(user, null);
    }

}
