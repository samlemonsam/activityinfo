package org.activityinfo.server.endpoint.rest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.database.transfer.RequestTransfer;
import org.activityinfo.model.database.transfer.TransferAuthorized;
import org.activityinfo.server.command.CommandTestCase;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.mail.MailSender;
import org.activityinfo.server.mail.MailSenderStubModule;
import org.activityinfo.store.spi.FormStorageProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@Modules(MailSenderStubModule.class)
@OnDataSet("/dbunit/clone-database.db.xml")
public class DatabaseResourceTest extends CommandTestCase {

    private final AuthenticatedUser alex = new AuthenticatedUser("XYZ", 1, "akbertram@gmail.com");
    private final AuthenticatedUser bavon = new AuthenticatedUser("XYZ", 2, "bavon@nrc.org");
    private final AuthenticatedUser other = new AuthenticatedUser("ZZZ", 1111, "other@other.org");

    private final int databaseId = 3;

    private DatabaseResource resource;

    @Inject
    private Provider<FormStorageProvider> formStorageProvider;

    @Inject
    private DispatcherSync dispatcher;

    @Inject
    private MailSender mailSender;

    @Before
    public void setUp() {
        resource = new DatabaseResource(formStorageProvider,
                dispatcher,
                new DatabaseProviderImpl(Providers.of(em)),
                Providers.of(em),
                mailSender,
                databaseId);
    }

    @Test
    public void requestTransfer() {
        RequestTransfer request = buildRequest();
        Response response = resource.requestTransfer(alex, request);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not generate request: { " + status + " }");
        }

        Database database = em.find(Database.class, databaseId);

        // Make sure token is generated and no ownership transfer has occurred
        assertTrue(database.getOwner().getId() == alex.getId());
        assertTrue(database.getTransferToken() != null);
        assertTrue(database.getTransferUser().getId() == bavon.getId());
    }

    private RequestTransfer buildRequest() {
        return new RequestTransfer(bavon.getEmail());
    }

    @Test
    public void multipleTransfer() {
        requestTransfer();
        try {
            requestTransfer();
        } catch (AssertionError conflict) {
            // Should reach here as request has not been resolved
            return;
        }
        throw new AssertionError("Server allowed multiple requests to be generated against single database");
    }

    @Test
    public void startTransferAsProposedOwner() {
        RequestTransfer request = buildRequest();
        Response response = resource.requestTransfer(alex, request);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not generate request: { " + status + " }");
        }

        Database database = em.find(Database.class, databaseId);
        String token = database.getTransferToken();

        TransferAuthorized transfer = new TransferAuthorized(alex.getId(), bavon.getId(), databaseId, token);
        response = resource.startTransfer(bavon, transfer);

        status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not start transfer: { " + status + ": " + response.getEntity() + " }");
        }

        database = em.find(Database.class, databaseId);

        // Make sure database has transferred to new owner and token is nulled
        assertTrue(database.getOwner().getId() == bavon.getUserId());
        assertTrue(database.getTransferToken() == null);
        assertTrue(database.getTransferUser() == null);
        assertTrue(database.getTransferRequestDate() == null);
    }

    @Test
    public void startTransferWithIncorrectToken() {
        RequestTransfer request = buildRequest();
        Response response = resource.requestTransfer(alex, request);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not generate request: { " + status + " }");
        }

        Database database = em.find(Database.class, databaseId);
        String token = database.getTransferToken();

        TransferAuthorized transfer = new TransferAuthorized(alex.getId(), bavon.getId(), databaseId, "WRONG TOKEN");
        response = resource.startTransfer(bavon, transfer);

        status = Response.Status.fromStatusCode(response.getStatus());

        if (status == Response.Status.OK) {
            throw new AssertionError("Should not be able to initiate transfer with incorrect token");
        }

        database = em.find(Database.class, databaseId);

        // Make sure database has not changed owner and token is still valid
        assertTrue(database.getOwner().getId() == alex.getUserId());
        assertTrue(database.getTransferToken() != null);
        assertTrue(database.getTransferToken().equals(token));
        assertTrue(database.getTransferUser().getId() == bavon.getUserId());
        assertTrue(database.getTransferRequestDate() != null);
    }

    @Test
    public void startTransferAsOriginalOwner() {
        RequestTransfer request = buildRequest();
        Response response = resource.requestTransfer(alex, request);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not generate request: { " + status + " }");
        }

        Database database = em.find(Database.class, databaseId);
        String token = database.getTransferToken();

        TransferAuthorized transfer = new TransferAuthorized(alex.getId(), bavon.getId(), databaseId, token);
        response = resource.startTransfer(alex, transfer);

        status = Response.Status.fromStatusCode(response.getStatus());

        if (status == Response.Status.OK) {
            throw new AssertionError("Should not be able to initiate transfer as original owner");
        }

        database = em.find(Database.class, databaseId);

        // Make sure database has not changed owner and token is still valid
        assertTrue(database.getOwner().getId() == alex.getUserId());
        assertTrue(database.getTransferToken() != null);
        assertTrue(database.getTransferToken().equals(token));
        assertTrue(database.getTransferUser() != null);
        assertTrue(database.getTransferRequestDate() != null);
    }

    @Test
    public void startTransferAsUnauthorizedUser() {
        RequestTransfer request = buildRequest();
        Response response = resource.requestTransfer(alex, request);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not generate request: { " + status + " }");
        }

        Database database = em.find(Database.class, databaseId);
        String token = database.getTransferToken();

        TransferAuthorized transfer = new TransferAuthorized(alex.getId(), bavon.getId(), databaseId, token);
        response = resource.startTransfer(other, transfer);

        status = Response.Status.fromStatusCode(response.getStatus());

        if (status == Response.Status.OK) {
            throw new AssertionError("Should not be able to initiate transfer as unauthorized user");
        }

        database = em.find(Database.class, databaseId);

        // Make sure database has not changed owner and token is still valid
        assertTrue(database.getOwner().getId() == alex.getUserId());
        assertTrue(database.getTransferToken() != null);
        assertTrue(database.getTransferToken().equals(token));
        assertTrue(database.getTransferUser() != null);
        assertTrue(database.getTransferRequestDate() != null);
    }

    @Test
    public void cancelTransferAsOriginalOwner() {
        RequestTransfer request = buildRequest();
        Response response = resource.requestTransfer(alex, request);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not generate request: { " + status + " }");
        }

        response = resource.cancelTransfer(alex, null);

        status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not cancel transfer: { " + status + ": " + response.getEntity() + " }");
        }

        Database database = em.find(Database.class, databaseId);

        // Make sure database has not changed owner and token is nulled
        assertTrue(database.getOwner().getId() == alex.getUserId());
        assertTrue(database.getTransferToken() == null);
        assertTrue(database.getTransferUser() == null);
        assertTrue(database.getTransferRequestDate() == null);
    }

    @Test
    public void cancelTransferAsProposedOwner() {
        RequestTransfer request = buildRequest();
        Response response = resource.requestTransfer(alex, request);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not generate request: { " + status + " }");
        }

        response = resource.cancelTransfer(bavon, null);

        status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not cancel transfer: { " + status + ": " + response.getEntity() + " }");
        }

        Database database = em.find(Database.class, databaseId);

        // Make sure database has not changed owner and token is nulled
        assertTrue(database.getOwner().getId() == alex.getUserId());
        assertTrue(database.getTransferToken() == null);
        assertTrue(database.getTransferUser() == null);
        assertTrue(database.getTransferRequestDate() == null);
    }

    @Test
    public void cancelTransferAsUnauthorizedUser() {
        RequestTransfer request = buildRequest();
        Response response = resource.requestTransfer(alex, request);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not generate request: { " + status + " }");
        }

        response = resource.cancelTransfer(other, request.toJson().toJson());

        status = Response.Status.fromStatusCode(response.getStatus());

        if (status == Response.Status.OK) {
            throw new AssertionError("Should not be able to cancel if an unauthorized user");
        }

        Database database = em.find(Database.class, databaseId);

        // Make sure database has not changed owner and token is still present
        assertTrue(database.getOwner().getId() == alex.getUserId());
        assertTrue(database.getTransferToken() != null);
        assertTrue(database.getTransferUser() != null);
        assertTrue(database.getTransferRequestDate() != null);
    }

}