package org.activityinfo.service.blob;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.code.appengine.awt.image.BufferedImage;
import com.google.code.appengine.imageio.ImageIO;
import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import org.activityinfo.core.shared.util.MimeTypeUtil;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.authentication.AuthenticationModuleStub;
import org.activityinfo.server.util.config.ConfigModuleStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author yuriyz on 11/12/2015.
 */
@Modules({
        GcsBlobFieldStorageServiceModule.class,
        AuthenticationModuleStub.class,
        ConfigModuleStub.class
})
@RunWith(InjectionSupport.class)
public class GcsBlobFieldStorageServiceTest {

    private static final String FILE_NAME = "goabout.png";

    private LocalServiceTestHelper localServiceTestHelper = new LocalServiceTestHelper(
            new LocalBlobstoreServiceTestConfig(), new LocalDatastoreServiceTestConfig());

    @Inject
    GcsBlobFieldStorageService blobService;

    private AuthenticatedUser user;
    private BlobId blobId;
    private ResourceId resourceId = ResourceId.generateId();

    @Before
    public final void uploadBlob() throws IOException {
        localServiceTestHelper.setUp();
        blobService.setTestBucketName();

        user = new AuthenticatedUser();
        blobId = BlobId.generate();
        blobService.put(user,
                "attachment;filename=" + FILE_NAME,
                MimeTypeUtil.mimeTypeFromFileName(FILE_NAME),
                blobId, resourceId,
                ByteSource.wrap(ByteStreams.toByteArray(GcsBlobFieldStorageServiceTest.class.getResourceAsStream("goabout.png"))));
    }

    @After
    public void tearDown() {
        localServiceTestHelper.tearDown();
    }

    @Test
    public void image() throws IOException {
        Response response = blobService.getImage(user, blobId, resourceId);
        assertEquals(response.getStatus(), 200);

        Object entity = response.getEntity();

        String directory = "target/blob-test/";
        createDirectory(directory);

        File imageFile = new File(directory + File.separator + FILE_NAME);

        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream((byte[]) entity));
        ImageIO.write(bufferedImage, MimeTypeUtil.fileExtension(FILE_NAME), imageFile);
        System.out.println(imageFile.getAbsolutePath());
    }

    @Test
    public void servingImageUrl() throws IOException {
        Response response = blobService.getImageUrl(new AuthenticatedUser(), blobId, resourceId);

        assertEquals(response.getStatus(), 200);
        assertTrue(!Strings.isNullOrEmpty((String) response.getEntity()));
    }

    @Test
    public void blobUrl() {
        Response response = blobService.getBlobUrl(new AuthenticatedUser(), blobId, resourceId);

        assertEquals(response.getStatus(), 200);
        assertTrue(!Strings.isNullOrEmpty((String) response.getEntity()));
    }

    @Test
    public void thumbnail() throws IOException {
        int width = 30;
        int height = 20;

        Response response = blobService.getThumbnail(user, blobId, resourceId, width, height);
        assertEquals(response.getStatus(), 200);

        Object entity = response.getEntity();

        String directory = "target/blob-test/thumbnail";
        createDirectory(directory);

        File imageFile = new File(directory + File.separator + FILE_NAME);

        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream((byte[]) entity));
        ImageIO.write(bufferedImage, MimeTypeUtil.fileExtension(FILE_NAME), imageFile);
        System.out.println(imageFile.getAbsolutePath());

        assertEquals(bufferedImage.getWidth(), width);

        // we don't check height because image scales depending on width
        //assertEquals(bufferedImage.getHeight(), height);
    }

    public static String createDirectory(String directory) {
        new File(directory).mkdir();
        return directory;
    }
}
