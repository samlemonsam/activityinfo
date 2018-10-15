package org.activityinfo.server.approval;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.util.Closeable;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.database.transfer.RequestTransfer;
import org.activityinfo.server.authentication.AuthTokenProvider;
import org.activityinfo.server.command.CommandTestCase;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;
import org.activityinfo.server.endpoint.rest.DatabaseProviderImpl;
import org.activityinfo.server.endpoint.rest.DatabaseResource;
import org.activityinfo.server.login.RestMockUtils;
import org.activityinfo.server.mail.MailSender;
import org.activityinfo.server.mail.MailSenderStubModule;
import org.activityinfo.store.spi.FormStorageProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;


@RunWith(InjectionSupport.class)
@Modules(MailSenderStubModule.class)
@OnDataSet("/dbunit/clone-database.db.xml")
public class ApprovalResourceTest extends CommandTestCase {


    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100),
                    new LocalTaskQueueTestConfig());

    private final AuthenticatedUser alex = new AuthenticatedUser("XYZ", 1, "akbertram@gmail.com");
    private final AuthenticatedUser bavon = new AuthenticatedUser("XYZ", 2, "bavon@nrc.org");
    private final AuthenticatedUser other = new AuthenticatedUser("ZZZ", 1111, "other@other.org");

    private final int databaseId = 3;

    private DatabaseResource databaseResource;
    private ApprovalResource approvalResource;
    private Closeable objectifyCloseable;

    @Inject
    private Provider<FormStorageProvider> formStorageProvider;

    @Inject
    private DispatcherSync dispatcher;

    @Inject
    private MailSender mailSender;

    @Inject
    private AuthTokenProvider authTokenProvider;

    @Inject
    private BillingAccountOracle billingOracle;


    private UriInfo uri;

    @Before
    public void setUp() throws URISyntaxException {
        databaseResource = new DatabaseResource(formStorageProvider,
                dispatcher,
                new DatabaseProviderImpl(Providers.of(em), billingOracle),
                Providers.of(em),
                mailSender,
                databaseId);

        approvalResource = new ApprovalResource(Providers.of(em),
                dispatcher,
                formStorageProvider,
                mailSender,
                authTokenProvider,
                billingOracle);

        uri = RestMockUtils.mockUriInfo("https://activityinfo.org/");
        helper.setUp();
        objectifyCloseable = ObjectifyService.begin();
    }

    @After
    public void tearDown() {
        helper.tearDown();
        objectifyCloseable.close();
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
        ObjectifyService.run(new VoidWork() {
            @Override
            public void vrun() {
                Response response = approvalResource.accept(uri, token);

                Response.Status status = Response.Status.fromStatusCode(response.getStatus());

                // Make sure we're redirected
                if (status != Response.Status.SEE_OTHER) {
                    throw new AssertionError("Could not accept and execute database transfer: { " + status + " : " + response.getEntity() + " }");
                }
            }
        });
    }

    @Test
    public void reject() {
        String token = requestTransfer();
        Response response = approvalResource.reject(token);

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());

        if (status != Response.Status.OK) {
            throw new AssertionError("Could not reject and cancel database transfer: { " + status + " : " + response.getEntity() + " }");
        }
    }


}