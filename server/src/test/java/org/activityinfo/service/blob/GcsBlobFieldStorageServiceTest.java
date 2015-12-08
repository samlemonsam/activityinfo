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
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.form.tree.AsyncFormTreeBuilder;
import org.activityinfo.core.shared.criteria.IdCriteria;
import org.activityinfo.core.shared.util.MimeTypeUtil;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.adapter.ResourceLocatorAdaptor;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.TFormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.server.authentication.AuthenticationModuleStub;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.junit.*;
import org.junit.runner.RunWith;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static org.activityinfo.core.client.PromiseMatchers.assertResolves;
import static org.activityinfo.model.legacy.CuidAdapter.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author yuriyz on 11/12/2015.
 */

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/schema1.db.xml")
public class GcsBlobFieldStorageServiceTest extends CommandTestCase2 {

    private static final String FILE_NAME = "goabout.png";

    private final LocalServiceTestHelper localServiceTestHelper = new LocalServiceTestHelper(
            new LocalBlobstoreServiceTestConfig(), new LocalDatastoreServiceTestConfig());

    @Inject
    GcsBlobFieldStorageService blobService;

    private AuthenticatedUser user;
    private AuthenticatedUser noAccessUser;
    private BlobId blobId;
    private ResourceId resourceId = CuidAdapter.activityFormClass(1);
    private ResourceLocatorAdaptor resourceLocator;

    @BeforeClass
    public static void setupI18N() {
        LocaleProxy.initialize();
        AuthenticationModuleStub.setUserId(1);
    }

    @Before
    public final void uploadBlob() throws IOException {
        localServiceTestHelper.setUp();
        blobService.setTestBucketName();
        resourceLocator = new ResourceLocatorAdaptor(getDispatcher());

        user = new AuthenticatedUser("x", 1, "user1@user.com");
        noAccessUser = new AuthenticatedUser("x", 3, "stefan@user.com");
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

    @Test(expected = WebApplicationException.class)
    public void imageNoPermission() throws IOException {
        blobService.getImage(noAccessUser, blobId, resourceId);
    }

    @Test
    public void servingImageUrl() throws IOException {
        Response response = blobService.getImageUrl(user, blobId, resourceId);

        assertEquals(response.getStatus(), 200);
        assertTrue(!Strings.isNullOrEmpty((String) response.getEntity()));
    }

    @Test(expected = WebApplicationException.class)
    public void servingImageUrlNoPermission() throws IOException {
        blobService.getImageUrl(noAccessUser, blobId, resourceId);
    }

    @Test
    public void blobUrl() {
        Response response = blobService.getBlobUrl(user, blobId, resourceId);

        assertEquals(response.getStatus(), 303);
        assertTrue(!Strings.isNullOrEmpty((String) response.getEntity()));
    }

    @Test(expected = WebApplicationException.class)
    public void blobUrlNoPermission() throws IOException {
        blobService.getBlobUrl(noAccessUser, blobId, resourceId);
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

    @Test(expected = WebApplicationException.class)
    public void thumbnailNoPermission() throws IOException {
        blobService.getThumbnail(noAccessUser, blobId, resourceId, 20, 20);
    }

    public static String createDirectory(String directory) {
        new File(directory).mkdir();
        return directory;
    }

    @Test
    @Ignore
    @OnDataSet("/dbunit/sites-simple1.db.xml")
    public void blobPermissionAttack() throws IOException {

        int activityId = 1;
        ResourceId formId = CuidAdapter.activityFormClass(activityId);

        blobId = BlobId.generate();
        blobService.put(user,
                "attachment;filename=" + FILE_NAME,
                MimeTypeUtil.mimeTypeFromFileName(FILE_NAME),
                blobId, formId,
                ByteSource.wrap(ByteStreams.toByteArray(GcsBlobFieldStorageServiceTest.class.getResourceAsStream("goabout.png"))));


        FormInstance instance = new FormInstance(CuidAdapter.cuid(SITE_DOMAIN, new KeyGenerator().generateInt()),
                formId);

        instance.set(indicatorField(1), 1);
        instance.set(indicatorField(2), 2);
        instance.set(locationField(activityId), locationInstanceId(1));
        instance.set(partnerField(activityId), partnerInstanceId(1));
        instance.set(projectField(activityId), projectInstanceId(1));
        instance.set(field(formId, START_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(field(formId, END_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(field(formId, COMMENT_FIELD), "My comment");

        assertResolves(resourceLocator.persist(instance));

        TFormTree formTree = new TFormTree(assertResolves(new AsyncFormTreeBuilder(resourceLocator).apply(formId)));
        InstanceQuery query = new InstanceQuery(Lists.newArrayList(formTree.getRootPaths()), new IdCriteria(instance.getId()));

//        Projection firstRead = singleSiteProjection(query);
//
//        assertEquals(new Quantity(1), firstRead.getValue(path(indicatorField(1))));
//        assertEquals(new Quantity(2), firstRead.getValue(path(indicatorField(2))));
//        assertEquals(new Quantity(3), firstRead.getValue(path(indicatorField(11))));
//        assertEquals(new Quantity(0.5), firstRead.getValue(path(indicatorField(12))));
//
//        // set indicators to null
//        instance.set(indicatorField(1).asString(), null);
//        instance.set(indicatorField(2).asString(), null);
//
//        // persist it
//        assertResolves(resourceLocator.persist(instance));
//
//        // read from server
//        Projection secondRead = singleSiteProjection(query);
//
//        assertEquals(null, secondRead.getValue(path(indicatorField(1))));
//        assertEquals(null, secondRead.getValue(path(indicatorField(2))));
//        assertEquals(new Quantity(0), secondRead.getValue(path(indicatorField(11))));
//        assertEquals(new Quantity(Double.NaN), secondRead.getValue(path(indicatorField(12)))); // make sure NaN is not returned |
    }
}
