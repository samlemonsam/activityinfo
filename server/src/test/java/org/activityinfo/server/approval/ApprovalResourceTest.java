package org.activityinfo.server.approval;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.database.transfer.RequestTransfer;
import org.activityinfo.server.command.CommandTestCase;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.endpoint.rest.DatabaseProviderImpl;
import org.activityinfo.server.endpoint.rest.DatabaseResource;
import org.activityinfo.server.mail.MailSender;
import org.activityinfo.server.mail.MailSenderStubModule;
import org.activityinfo.store.spi.FormStorageProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;


@RunWith(InjectionSupport.class)
@Modules(MailSenderStubModule.class)
@OnDataSet("/dbunit/clone-database.db.xml")
public class ApprovalResourceTest extends CommandTestCase {

    private final AuthenticatedUser alex = new AuthenticatedUser("XYZ", 1, "akbertram@gmail.com");
    private final AuthenticatedUser bavon = new AuthenticatedUser("XYZ", 2, "bavon@nrc.org");
    private final AuthenticatedUser other = new AuthenticatedUser("ZZZ", 1111, "other@other.org");

    private final int databaseId = 3;

    private DatabaseResource databaseResource;
    private ApprovalResource approvalResource;

    @Inject
    private Provider<FormStorageProvider> formStorageProvider;

    @Inject
    private DispatcherSync dispatcher;

    @Inject
    private MailSender mailSender;

    @Before
    public void setUp() {
        databaseResource = new DatabaseResource(formStorageProvider,
                dispatcher,
                new DatabaseProviderImpl(Providers.of(em)),
                Providers.of(em),
                mailSender,
                databaseId);

        approvalResource = new ApprovalResource(Providers.of(em),
                dispatcher,
                formStorageProvider,
                mailSender);
    }

    private String requestTransfer() {
        RequestTransfer request = new RequestTransfer(bavon.getEmail());
        databaseResource.requestTransfer(alex, request);
        Database database = em.find(Database.class, databaseId);
        return database.getTransferToken();
    }

    @Test
    public void accept() {
        String token = requestTransfer();
        Response response = approvalResource.accept(bavon, token);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not accept and execute database transfer: { " + status + " : " + response.getEntity() + " }");
        }
    }

    @Test
    public void acceptAsOriginalOwner() {
        String token = requestTransfer();
        Response response = approvalResource.accept(alex, token);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status == Response.Status.OK) {
            throw new AssertionError("Should not be able to accept and execute database transfer: { " + status + " : " + response.getEntity() + " }");
        }
    }

    @Test
    public void reject() {
        String token = requestTransfer();
        Response response = approvalResource.reject(bavon, token);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not reject and cancel database transfer: { " + status + " : " + response.getEntity() + " }");
        }
    }

    @Test
    public void rejectAsOriginalOwner() {
        String token = requestTransfer();
        Response response = approvalResource.reject(alex, token);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status == Response.Status.OK) {
            throw new AssertionError("Should not be able to reject and cancel database transfer: { " + status + " : " + response.getEntity() + " }");
        }
    }


}