package org.activityinfo.server.blob;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.*;
import com.google.appengine.tools.cloudstorage.*;
import com.google.appengine.tools.cloudstorage.GcsFileOptions.Builder;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.command.handler.PermissionOracle;
import org.activityinfo.server.database.hibernate.entity.Activity;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.spi.BlobAuthorizer;
import org.activityinfo.store.spi.BlobId;
import org.apache.commons.io.IOUtils;
import org.joda.time.Duration;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;

@Path("/service/blob")
public class GcsBlobFieldStorageService implements BlobFieldStorageService, BlobAuthorizer {

    private static final int ONE_MEGABYTE = 1 << 20;
    private static final Logger LOGGER = Logger.getLogger(GcsBlobFieldStorageService.class.getName());

    public static final int MAX_BLOB_LENGTH_IN_MEGABYTES = 10;

    private AppIdentityService appIdentityService;
    private final Provider<EntityManager> em;

    private String bucketName;

    @Inject
    public GcsBlobFieldStorageService(DeploymentConfiguration config, Provider<EntityManager> em) {
        this.bucketName = config.getBlobServiceBucketName();
        this.em = em;

        try {
            if (Strings.isNullOrEmpty(bucketName)) {
                LOGGER.log(Level.SEVERE, "Failed to start blob service. Bucket name is blank. Please provide bucket name in configuration file with property "
                        + DeploymentConfiguration.BLOBSERVICE_GCS_BUCKET_NAME);
                return;
            }
            this.appIdentityService = /*DeploymentEnvironment.isAppEngineDevelopment() ?
                    new DevAppIdentityService(config) : */AppIdentityServiceFactory.getAppIdentityService();
            LOGGER.info("Service account: " + appIdentityService.getServiceAccountName() + ", bucketName: " + bucketName);
        } catch (Exception e) {
            // ignore: fails in local tests, bug in LocalServiceTestHelper?

            // also we want to prevent situation when exception in storage leads to server start failure
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @GET
    @Path("{blobId}/{resourceId}/blobUrl")
    @Override
    public Response getBlobUrl(@InjectParam AuthenticatedUser user,
                               @PathParam("blobId") BlobId blobId,
                               @PathParam("resourceId") ResourceId resourceId) {

        assertNotAnonymousUser(user);
        assertHasAccess(user, blobId, resourceId);
        assertBlobExists(blobId);

        try {
            GcsAppIdentityServiceUrlSigner signer = new GcsAppIdentityServiceUrlSigner();
            String url = signer.getSignedUrl("GET", bucketName + "/" + blobId.asString());
            return Response.seeOther(new URI(url)).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void put(AuthenticatedUser user, String contentDisposition, String mimeType, BlobId blobId,
                    ResourceId resourceId,
                    InputStream inputStream) throws IOException {

        ResourceId userId = CuidAdapter.userId(user.getUserId());

        assertNotAnonymousUser(user);
        if (!hasAccessToResource(userId, resourceId)) {
            throw new WebApplicationException(UNAUTHORIZED);
        }

        GcsFileOptions gcsFileOptions = new Builder().
                contentDisposition(contentDisposition).
                mimeType(mimeType).
                addUserMetadata(GcsUploadCredentialBuilder.X_CREATOR, userId.asString()).
                addUserMetadata(GcsUploadCredentialBuilder.X_OWNER, resourceId.asString()).
                build();
        GcsOutputChannel channel = GcsServiceFactory.createGcsService().createOrReplace(new GcsFilename(bucketName, blobId.asString()), gcsFileOptions);

        try (OutputStream outputStream = Channels.newOutputStream(channel)) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    @GET
    @Path("{blobId}/{resourceId}/image")
    @Override
    public Response getImage(@InjectParam AuthenticatedUser user,
                             @PathParam("blobId") BlobId blobId,
                             @PathParam("resourceId") ResourceId resourceId) throws IOException {

        assertNotAnonymousUser(user);
        assertHasAccess(user, blobId, resourceId);
        assertBlobExists(blobId);

        GcsFilename gcsFilename = new GcsFilename(bucketName, blobId.asString());

        GcsService gcsService = GcsServiceFactory.createGcsService();
        GcsInputChannel gcsInputChannel = gcsService.openPrefetchingReadChannel(gcsFilename, 0, ONE_MEGABYTE);
        GcsFileMetadata metadata = gcsService.getMetadata(gcsFilename);

        try (InputStream inputStream = Channels.newInputStream(gcsInputChannel)) {
            return Response.ok(ByteStreams.toByteArray(inputStream)).type(metadata.getOptions().getMimeType()).build();
        }
    }

    @GET
    @Path("{blobId}/{resourceId}/imageUrl")
    @Override
    public Response getImageUrl(@InjectParam AuthenticatedUser user,
                                @PathParam("blobId") BlobId blobId,
                                @PathParam("resourceId") ResourceId resourceId) throws IOException {

        assertNotAnonymousUser(user);
        assertHasAccess(user, blobId, resourceId);
        assertBlobExists(blobId);

        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        String url = imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(blobKey(blobId)).secureUrl(true));

        return Response.ok(url).type(MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("{blobId}/{resourceId}/thumbnail")
    @Override
    public Response getThumbnail(@InjectParam AuthenticatedUser user,
                                 @PathParam("blobId") BlobId blobId,
                                 @PathParam("resourceId") ResourceId resourceId,
                                 @QueryParam("width") int width,
                                 @QueryParam("height") int height) {

        assertNotAnonymousUser(user);
        assertHasAccess(user, blobId, resourceId);
        assertBlobExists(blobId);

        ImagesService imagesService = ImagesServiceFactory.getImagesService();

        Image image = ImagesServiceFactory.makeImageFromBlob(blobKey(blobId));

        Transform resize = ImagesServiceFactory.makeResize(width, height);
        Image newImage = imagesService.applyTransform(resize, image);

        String mimeType = "image/" + newImage.getFormat().name().toLowerCase();
        return Response.ok(newImage.getImageData()).type(mimeType).build();
    }

    @GET
    @Path("{blobId}/{resourceId}/exists")
    public Response exists(@InjectParam AuthenticatedUser user,
                           @PathParam("blobId") BlobId blobId,
                           @PathParam("resourceId") ResourceId resourceId) {
        assertNotAnonymousUser(user);
        assertHasAccess(user, blobId, resourceId);
        assertBlobExists(blobId);
        return Response.ok().build();
    }

    public BlobKey blobKey(BlobId blobId) {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        return blobstoreService.createGsBlobKey("/gs/" + bucketName + "/" + blobId.asString());
    }

    @POST
    @Path("credentials/{blobId}/{resourceId}/{fileName}")
    @Override
    public Response getUploadCredentials(@InjectParam AuthenticatedUser user,
                                         @PathParam("blobId") BlobId blobId,
                                         @PathParam("resourceId") ResourceId resourceId,
                                         @PathParam("fileName") String fileName) {
        assertNotAnonymousUser(user);

        UploadCredentials uploadCredentials = new GcsUploadCredentialBuilder(appIdentityService, fileName).
                setCreatorId(CuidAdapter.userId(user.getUserId())).
                setOwnerId(resourceId).
                setBucket(bucketName).
                setKey(blobId.asString()).
                setMaxContentLengthInMegabytes(MAX_BLOB_LENGTH_IN_MEGABYTES).
                expireAfter(Duration.standardMinutes(10)).
                build();

        return Response.ok(uploadCredentials.asJson()).
                type(MediaType.APPLICATION_JSON).
                build();
    }

    public void assertHasAccess(AuthenticatedUser user, BlobId blobId, ResourceId resourceId) {
        try {
            GcsFileMetadata metadata = GcsServiceFactory.createGcsService().getMetadata(new GcsFilename(bucketName, blobId.asString()));
            if (hasAccess(CuidAdapter.userId(user.getUserId()), resourceId, blobId, metadata)) {
                return;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        throw new WebApplicationException(UNAUTHORIZED);
    }

    public boolean hasAccess(ResourceId userId, ResourceId resourceId, BlobId blobId, GcsFileMetadata metadata) {
        if (metadata == null) {
            return false;
        }

        String ownerIdStr = metadata.getOptions().getUserMetadata().get(GcsUploadCredentialBuilder.X_OWNER);
        String creatorIdStr = metadata.getOptions().getUserMetadata().get(GcsUploadCredentialBuilder.X_CREATOR);

        LOGGER.finest(String.format("Blob: %s, owner: %s, creator: %s", blobId.asString(), ownerIdStr, creatorIdStr));

        Preconditions.checkNotNull(ownerIdStr, "Owner of blob is null.");
        Preconditions.checkNotNull(creatorIdStr, "Creator of blob is null.");

        if (userId.equals(ResourceId.valueOf(creatorIdStr))) { // owner
            return true;
        }
        return hasAccessToResource(userId, resourceId);
    }

    private boolean hasAccessToResource(ResourceId userId, ResourceId resourceId) {
        if (resourceId.getDomain() == CuidAdapter.ACTIVITY_DOMAIN) {
            Activity activity = em.get().find(Activity.class, CuidAdapter.getLegacyIdFromCuid(resourceId));

            if (PermissionOracle.using(em.get()).isViewAllowed(activity.getDatabase(), em.get().getReference(User.class, CuidAdapter.getLegacyIdFromCuid(userId)))) {
                return true;
            }
        } else {
            throw new UnsupportedOperationException("Blob owner is not supported, ownerId: " + resourceId);
        }
        return false;
    }

    public void assertNotAnonymousUser(@InjectParam AuthenticatedUser user) {
        if (appIdentityService == null) {
            throw new WebApplicationException(SERVICE_UNAVAILABLE);
        }
        if (user == null || user.isAnonymous()) {
            throw new WebApplicationException(UNAUTHORIZED);
        }
    }

    public void setTestBucketName() {
        if (bucketName == null) {
            bucketName = appIdentityService.getDefaultGcsBucketName();
        }
    }

    public void assertBlobExists(BlobId blobId) {
        try {
            // fetch just one byte of blob to make sure it exists
            BlobstoreServiceFactory.getBlobstoreService().fetchData(blobKey(blobId), 0, 1);
        } catch (Exception e) {
            throw new WebApplicationException(NOT_FOUND);
        }
    }

    public String getBucketName() {
        return bucketName;
    }

    @Override
    public boolean isOwner(int userId, String blobId) {
        GcsFileMetadata metadata = null;
        try {
            metadata = GcsServiceFactory.createGcsService().getMetadata(new GcsFilename(bucketName, blobId));
        } catch (IOException e) {
            throw new RuntimeException("Failed to check blob ownership", e);
        }
        String creatorIdStr = metadata.getOptions().getUserMetadata().get(GcsUploadCredentialBuilder.X_CREATOR);
        return CuidAdapter.userId(userId).asString().equals(creatorIdStr);
    }
}
