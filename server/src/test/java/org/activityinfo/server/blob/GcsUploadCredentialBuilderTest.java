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
package org.activityinfo.server.blob;

import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.BlobId;
import org.joda.time.Duration;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Map;

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

}
