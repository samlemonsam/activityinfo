package org.activityinfo.service.blob;

import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.joda.time.Period;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Map;

import static com.google.common.net.MediaType.PNG;

public class GcsUploadCredentialBuilderTest {

    @Test
    public void test() throws Exception {

        UploadCredentials credentials = new GcsUploadCredentialBuilder(new TestingIdentityService())
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

}
