package org.activityinfo.service.blob;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.util.blob.DevAppIdentityService;
import org.activityinfo.service.DeploymentConfiguration;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Properties;

import static com.google.common.net.MediaType.PNG;

public class GcsUploadCredentialBuilderTest {

    private static final String PRIVATE_KEY_FILE_PATH = "U:\\own\\github\\repo\\activityinfo\\production2\\server\\src\\main\\resources\\org\\activityinfo\\server\\util\\blob\\ai-staging-e41defb26a88.p12";

    @Test
    public void test() throws Exception {

        UploadCredentials credentials = new GcsUploadCredentialBuilder(new TestingIdentityService(PRIVATE_KEY_FILE_PATH), "file.png").
                setCreatorId(CuidAdapter.userId(1)).
                setOwnerId(ResourceId.generateId()).
                setBucket("ai-dev-field-blob-test").
                setKey(BlobId.generate().asString()).
                setMaxContentLengthInMegabytes(10).
                expireAfter(Duration.standardMinutes(5)).
                build();

        FormDataMultiPart form = new FormDataMultiPart();

        for (Map.Entry<String, String> entry : credentials.getFormFields().entrySet()) {
            form.field(entry.getKey(), entry.getValue());
        }

        form.field("file", Resources.asByteSource(Resources.getResource(getClass(), "goabout.png")).read(),
                MediaType.valueOf(PNG.toString()));

        Client.create()
                .resource(credentials.getUrl())
                .entity(form, MediaType.MULTIPART_FORM_DATA_TYPE)
                .post();

    }

    @Test
    @Ignore
    public void policyDocument() {
        GcsPolicyBuilder gcsPolicyBuilder = new GcsPolicyBuilder().expiresAfter(Duration.standardMinutes(10));
        System.out.println(gcsPolicyBuilder.toJson());
    }

    @Test
    @Ignore // run manually only
    public void identityService() {
        Properties properties = new Properties();
        properties.setProperty("service.account.p12.classpath.fileName", "ai-staging-e41defb26a88.p12");
        properties.setProperty("service.account.name", "210521273034-q311jj20r5ep1siksedr9fpmta7k5a6i@developer.gserviceaccount.com");
        properties.setProperty("service.account.p12.key.password", "notasecret");
//        properties.setProperty("service.account.p12.key.path", "c:\\Users\\admin\\ai-staging-e41defb26a88.p12");
        properties.setProperty("service.account.p12.key.path", "U:\\own\\github\\repo\\activityinfo\\production2\\server\\src\\main\\resources\\org\\activityinfo\\server\\util\\blob\\ai-staging-e41defb26a88.p12");

        AppIdentityService identityService = new DevAppIdentityService(new DeploymentConfiguration(properties));
        Assert.assertNotNull(identityService.getServiceAccountName());
    }

}
