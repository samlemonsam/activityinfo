package org.activityinfo.server.approval;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.model.database.transfer.TransferAuthorized;
import org.activityinfo.model.database.transfer.TransferDecision;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.server.authentication.AuthTokenProvider;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;
import org.activityinfo.server.endpoint.rest.DatabaseResource;
import org.activityinfo.server.mail.MailSender;
import org.activityinfo.store.spi.UserDatabaseProvider;
import org.activityinfo.store.query.UsageTracker;
import org.activityinfo.store.spi.FormStorageProvider;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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
    private AuthTokenProvider authTokenProvider;
    private UserDatabaseProvider userDatabaseProvider;
    private BillingAccountOracle billingOracle;

    @Inject
    public ApprovalResource(Provider<EntityManager> entityManager,
                            DispatcherSync dispatcher,
                            Provider<FormStorageProvider> catalog,
                            MailSender mailSender,
                            AuthTokenProvider authTokenProvider,
                            UserDatabaseProvider userDatabaseProvider,
                            BillingAccountOracle billingOracle) {
        this.entityManager = entityManager;
        this.dispatcher = dispatcher;
        this.catalog = catalog;
        this.mailSender = mailSender;
        this.authTokenProvider = authTokenProvider;
        this.userDatabaseProvider = userDatabaseProvider;
        this.billingOracle = billingOracle;
    }

    @GET
    @Path("/accept")
    public Response accept(@Context UriInfo uri, @QueryParam("token") String token) {
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

        TransferAuthorized transfer = new TransferAuthorized(currentOwner.getId(), proposedOwner.getId(), database.getId(), token);

        UsageTracker.track(proposedOwner.getId(), "db_transfer_accept", database.getResourceId());

        DatabaseResource resource = new DatabaseResource(catalog,
                dispatcher,
                userDatabaseProvider,
                entityManager,
                mailSender,
                billingOracle,
                CuidAdapter.databaseId(database.getId()).asString());

        return resource.startTransfer(uri, authTokenProvider, transfer);
    }

    @GET
    @Path("/reject")
    public Response reject(@QueryParam("token") String token) {
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

        UsageTracker.track(proposedOwner.getId(), "db_transfer_reject", database.getResourceId());

        DatabaseResource resource = new DatabaseResource(catalog,
                dispatcher,
                userDatabaseProvider,
                entityManager,
                mailSender,
                billingOracle,
                CuidAdapter.databaseId(database.getId()).asString());

        return resource.cancelTransfer(proposedOwner.asAuthenticatedUser(), TransferDecision.rejected());
    }

}
