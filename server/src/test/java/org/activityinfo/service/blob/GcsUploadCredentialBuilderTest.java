package org.activityinfo.service.blob;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.activityinfo.server.util.blob.DevAppIdentityService;
import org.activityinfo.service.DeploymentConfiguration;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Properties;

import static com.google.common.net.MediaType.PNG;

public class GcsUploadCredentialBuilderTest {

    private static final String PRIVATE_KEY_FILE_PATH = "u:\\own\\github\\repo\\activityinfo\\production2\\BeDataDriven Development-e30eef9283cd.p12";

    @Test
    public void test() throws Exception {

        UploadCredentials credentials = new GcsUploadCredentialBuilder(new TestingIdentityService(PRIVATE_KEY_FILE_PATH))
                .setBucket("ai-dev-field-blob-test")
                .setKey(BlobId.generate().asString())
                .setMaxContentLengthInMegabytes(10)
                .expireAfter(Period.minutes(5))
                .build();

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

    //@Test // run manually only
    public void identityService() {
        Properties properties = new Properties();
        properties.setProperty("service.account.p12.classpath.fileName", "BeDataDriven Development-e30eef9283cd.p12");
        properties.setProperty("service.account.name", "135288259907-k64g5vuv9en1o89on1ru16hrusvimn9t@developer.gserviceaccount.com");
        properties.setProperty("service.account.p12.key.password", "notasecret");
        properties.setProperty("service.account.p12.key.path", "c:\\Users\\admin\\BeDataDriven Development-e30eef9283cd.p12");

        AppIdentityService identityService = new DevAppIdentityService(new DeploymentConfiguration(properties));
        Assert.assertNotNull(identityService.getServiceAccountName());
    }

}
