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

import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.code.appengine.awt.image.BufferedImage;
import com.google.code.appengine.imageio.ImageIO;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.adapter.ActivityInfoClientAsyncStub;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.server.authentication.AuthenticationModuleStub;
import org.activityinfo.server.database.DatabaseModule;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.endpoint.gwtrpc.GwtRpcModule;
import org.activityinfo.server.util.TemplateModule;
import org.activityinfo.server.util.config.ConfigModuleStub;
import org.activityinfo.store.spi.BlobId;
import org.activityinfo.store.spi.UserDatabaseProvider;
import org.activityinfo.ui.client.component.form.field.attachment.MimeTypeUtil;
import org.activityinfo.ui.client.dispatch.ResourceLocatorAdaptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static org.activityinfo.model.legacy.CuidAdapter.*;
import static org.activityinfo.promise.PromiseMatchers.assertResolves;
import static org.junit.Assert.*;

/**
 * @author yuriyz on 11/12/2015.
 */

@RunWith(InjectionSupport.class)
@Modules({
        TestHibernateModule.class,
        TemplateModule.class,
        GwtRpcModule.class,
        AuthenticationModuleStub.class,
        ConfigModuleStub.class,
        GcsBlobFieldStorageServiceModule.class,
        DatabaseModule.class
})
@OnDataSet("/dbunit/schema1.db.xml")
public class GcsBlobFieldStorageServiceTest {

    @Inject
    private Injector injector;

    private static final String FILE_NAME = "goabout.png";
    public static final int USER_WITHOUT_ACCESS_TO_DB_1 = 22;

    private final LocalServiceTestHelper localServiceTestHelper = new LocalServiceTestHelper(
            new LocalBlobstoreServiceTestConfig(), new LocalDatastoreServiceTestConfig().
            setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

    private GcsBlobFieldStorageService blobService;
    private ResourceLocatorAdaptor locator;

    private AuthenticatedUser user;
    private AuthenticatedUser noAccessUser;
    private BlobId blobId;
    private ResourceId resourceId = CuidAdapter.activityFormClass(1);
    private Closeable ofy;


    @Before
    public final void uploadBlob() throws IOException {

        localServiceTestHelper.setUp();
        ofy = ObjectifyService.begin();

        blobService = injector.getInstance(GcsBlobFieldStorageService.class);
        blobService.setTestBucketName();

        locator = new ResourceLocatorAdaptor(
                new ActivityInfoClientAsyncStub(injector.getProvider(EntityManager.class),
                        injector.getInstance(UserDatabaseProvider.class),
                        blobService));

        AuthenticationModuleStub.setUserId(1);

        user = new AuthenticatedUser("x", 1, "user1@user.com");
        noAccessUser = new AuthenticatedUser("x", 3, "stefan@user.com");
        blobId = BlobId.generate();
        blobService.put(user,
                "attachment;filename=" + FILE_NAME,
                MimeTypeUtil.mimeTypeFromFileName(FILE_NAME),
                blobId, resourceId,
                GcsBlobFieldStorageServiceTest.class.getResourceAsStream("goabout.png"));
    }

    @After
    public void tearDown() {
        ofy.close();
        localServiceTestHelper.tearDown();
    }

    @Test
    public void image() throws IOException {
        Response response = blobService.getImage(user, blobId, resourceId);
        assertEquals(200, response.getStatus());

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

        assertEquals(200, response.getStatus());
        assertTrue(!Strings.isNullOrEmpty((String) response.getEntity()));
    }

    @Test(expected = WebApplicationException.class)
    public void servingImageUrlNoPermission() throws IOException {
        blobService.getImageUrl(noAccessUser, blobId, resourceId);
    }

    @Test
    public void blobUrl() {
        Response response = blobService.getBlobUrl(user, blobId, resourceId);

        assertEquals(303, response.getStatus());
        assertNotNull(response.getMetadata().getFirst("Location"));
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
        assertEquals(200, response.getStatus());

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

    /**
     * 1. user1 : persist blob with FormInstance1 (FormClass1) user1
     * 2. user2 : persist the same blob with FormInstance2 (FormClass2) -> try to steal blob access
     */
    @Test
    @OnDataSet("/dbunit/sites-simple-blob-security.db.xml")
    public void blobPermissionAttack() throws IOException {

        blobService.setTestBucketName();

        int activityId = 1;
        int databaseId = 1;
        int locationType = 10;

        ResourceId attachmentFieldId = ResourceId.generateFieldId(AttachmentType.TYPE_CLASS);
        FormClass formClass = addAttachmentField(activityId, attachmentFieldId);

        blobId = BlobId.generate();
        blobService.put(user,
                "attachment;filename=" + FILE_NAME,
                MimeTypeUtil.mimeTypeFromFileName(FILE_NAME),
                blobId, formClass.getId(),
                GcsBlobFieldStorageServiceTest.class.getResourceAsStream("goabout.png"));

        TypedFormRecord instance = new TypedFormRecord(CuidAdapter.cuid(SITE_DOMAIN, new KeyGenerator().generateInt()),
                formClass.getId());

        Attachment attachment = new Attachment();
        attachment.setMimeType(MimeTypeUtil.mimeTypeFromFileName(FILE_NAME));
        attachment.setBlobId(blobId.asString());
        attachment.setFilename(FILE_NAME);

        AttachmentValue attachmentValue = new AttachmentValue();
        attachmentValue.getValues().add(attachment);

        instance.set(indicatorField(1), 1);
        instance.set(indicatorField(2), 2);
        instance.set(attachmentFieldId, attachmentValue);
        instance.set(locationField(activityId), locationRef(CuidAdapter.locationFormClass(locationType), 1));
        instance.set(partnerField(activityId), partnerRef(databaseId, 1));
        instance.set(projectField(activityId), projectRef(databaseId, 1));
        instance.set(field(formClass.getId(), START_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(field(formClass.getId(), END_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(field(formClass.getId(), COMMENT_FIELD), "My comment");

        assertResolves(locator.persist(instance));

        assertInstanceExists(formClass.getId(), instance.getId());

        AuthenticationModuleStub.setUserId(USER_WITHOUT_ACCESS_TO_DB_1);

        int anotherActivityId = 32;
        ResourceId newAttachmentFieldId = ResourceId.generateFieldId(AttachmentType.TYPE_CLASS);
        addAttachmentField(anotherActivityId, newAttachmentFieldId);

        instance.setId(CuidAdapter.cuid(SITE_DOMAIN, new KeyGenerator().generateInt()));
        instance.setClassId(CuidAdapter.activityFormClass(anotherActivityId));
        instance.set(newAttachmentFieldId, attachmentValue);
        instance.set(field(instance.getFormId(), START_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(field(instance.getFormId(), END_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(partnerField(anotherActivityId), partnerRef(databaseId, 1));

        boolean persisted = true;
        try {
            assertResolves(locator.persist(instance)); // this must fail because of blob permission check
        } catch (RuntimeException e) {
            e.printStackTrace();
            persisted = false;
        }

        assertFalse("Access to blob is stolen! Permissions check for blobs is broken.", persisted);

    }

    private FormClass addAttachmentField(int activityId, ResourceId attachmentFieldId) {
        FormClass formClass = assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(activityId)));
        return addAttachmentField(formClass, attachmentFieldId);
    }

    private TypedFormRecord assertInstanceExists(ResourceId formId, ResourceId instanceId) {
        return assertResolves(locator.getFormInstance(formId, instanceId));
    }

    @Test
    public void subformBlobUpload() throws IOException {
        blobService.setTestBucketName();

        FormClass subForm = setupFormWithSubformAttachmentField();

        blobId = BlobId.generate();
        blobService.put(user,
                "attachment;filename=" + FILE_NAME,
                MimeTypeUtil.mimeTypeFromFileName(FILE_NAME),
                blobId, subForm.getId(),
                GcsBlobFieldStorageServiceTest.class.getResourceAsStream("goabout.png"));
    }

    @Test(expected = WebApplicationException.class)
    public void subformBlobUploadUnauthorized() throws IOException {
        blobService.setTestBucketName();

        FormClass subForm = setupFormWithSubformAttachmentField();

        blobId = BlobId.generate();
        blobService.put(noAccessUser,
                "attachment;filename=" + FILE_NAME,
                MimeTypeUtil.mimeTypeFromFileName(FILE_NAME),
                blobId, subForm.getId(),
                GcsBlobFieldStorageServiceTest.class.getResourceAsStream("goabout.png"));
    }

    private FormClass setupFormWithSubformAttachmentField() {
        int databaseId = 1;

        FormClass testForm = new FormClass(ResourceId.valueOf("TEST_FORM"));
        testForm.setLabel("Test Form with Sub-Form");
        testForm.setDatabaseId(databaseId);

        FormClass testSubForm = new FormClass(ResourceId.generateId());
        testSubForm.setLabel("Test Sub-Form");
        testSubForm.setDatabaseId(databaseId);
        testSubForm.setSubFormKind(SubFormKind.REPEATING);
        testSubForm.setParentFormId(testForm.getId());

        testForm.addElement(new FormField(ResourceId.valueOf("SF"))
                .setLabel("Sub-Form")
                .setType(new SubFormReferenceType(testSubForm.getId())));

        assertResolves(locator.persist(testSubForm));
        assertResolves(locator.persist(testForm));

        ResourceId attachmentFieldId = ResourceId.generateFieldId(AttachmentType.TYPE_CLASS);
        return addAttachmentField(testSubForm.getId(), attachmentFieldId);
    }

    private FormClass addAttachmentField(ResourceId formId, ResourceId attachmentFieldId) {
        FormClass formClass = assertResolves(locator.getFormClass(formId));
        return addAttachmentField(formClass, attachmentFieldId);
    }

    private FormClass addAttachmentField(FormClass formClass, ResourceId attachmentFieldId) {
        formClass.addElement(new FormField(attachmentFieldId)
                .setLabel("Attachment")
                .setType(AttachmentType.TYPE_CLASS.createType())
                .setVisible(true));

        assertResolves(locator.persist(formClass));
        return assertResolves(locator.getFormClass(formClass.getId())); // re-fetch
    }

}
