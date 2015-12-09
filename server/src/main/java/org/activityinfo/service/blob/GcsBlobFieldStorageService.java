package org.activityinfo.service.blob;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.*;
import com.google.appengine.tools.cloudstorage.*;
import com.google.appengine.tools.cloudstorage.GcsFileOptions.Builder;
import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.DeploymentEnvironment;
import org.activityinfo.server.command.handler.PermissionOracle;
import org.activityinfo.server.database.hibernate.entity.Activity;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.util.blob.DevAppIdentityService;
import org.activityinfo.service.DeploymentConfiguration;
import org.activityinfo.service.gcs.GcsAppIdentityServiceUrlSigner;
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

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path("/service/blob")
public class GcsBlobFieldStorageService implements BlobFieldStorageService {

    private static final int ONE_MEGABYTE = 1 << 20;
    private static final Logger LOGGER = Logger.getLogger(GcsBlobFieldStorageService.class.getName());

    public static final int MAX_BLOB_LENGTH_IN_MEGABYTES = 10;

    private final AppIdentityService appIdentityService;
    private final EntityManager em;

    private String bucketName;

    @Inject
    public GcsBlobFieldStorageService(DeploymentConfiguration config, EntityManager em) {
        this.bucketName = config.getBlobServiceBucketName();
        this.em = em;

        this.appIdentityService = DeploymentEnvironment.isAppEngineDevelopment() ?
                new DevAppIdentityService(config) : AppIdentityServiceFactory.getAppIdentityService();

        try {
            LOGGER.info("Service account: " + appIdentityService.getServiceAccountName());
        } catch (ApiProxy.CallNotFoundException e) {
            // ignore: fails in local tests, bug in LocalServiceTestHelper?
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

    @Override
    public void put(AuthenticatedUser user, String contentDisposition, String mimeType, BlobId blobId,
                    ResourceId resourceId,
                    ByteSource byteSource) throws IOException {

        ResourceId userId = CuidAdapter.userId(user.getUserId());

        assertNotAnonymousUser(user);
        if (!hasAccessToResource(userId, resourceId)) {
            throw new WebApplicationException(UNAUTHORIZED);
        }

        GcsFileOptions gcsFileOptions = new Builder().
                contentDisposition(contentDisposition).
                mimeType(mimeType).
                addUserMetadata(GcsUploadCredentialBuilder.X_GOOG_META_CREATOR, userId.asString()).
                addUserMetadata(GcsUploadCredentialBuilder.X_GOOG_META_OWNER, resourceId.asString()).
                build();
        GcsOutputChannel channel = GcsServiceFactory.createGcsService().createOrReplace(new GcsFilename(bucketName, blobId.asString()), gcsFileOptions);

        try (OutputStream outputStream = Channels.newOutputStream(channel)) {
            byteSource.copyTo(outputStream);
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

        GcsInputChannel gcsInputChannel = GcsServiceFactory.createGcsService().openPrefetchingReadChannel(gcsFilename, 0, ONE_MEGABYTE);
        GcsFileMetadata metadata = GcsServiceFactory.createGcsService().getMetadata(gcsFilename);

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

    private BlobKey blobKey(BlobId blobId) {
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

    private void assertHasAccess(AuthenticatedUser user, BlobId blobId, ResourceId resourceId) {
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

    public boolean hasAccess(ResourceId userId, BlobId blobId) {
        try {
            GcsFileMetadata metadata = GcsServiceFactory.createGcsService().getMetadata(new GcsFilename(bucketName, blobId.asString()));
            ResourceId resourceId = ResourceId.valueOf(metadata.getOptions().getUserMetadata().get(GcsUploadCredentialBuilder.X_GOOG_META_OWNER));
            return hasAccess(userId, resourceId, blobId, metadata);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    public boolean hasAccess(ResourceId userId, ResourceId resourceId, BlobId blobId, GcsFileMetadata metadata) {
        String ownerIdStr = metadata.getOptions().getUserMetadata().get(GcsUploadCredentialBuilder.X_GOOG_META_OWNER);
        String creatorIdStr = metadata.getOptions().getUserMetadata().get(GcsUploadCredentialBuilder.X_GOOG_META_CREATOR);

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
            Activity activity = em.find(Activity.class, CuidAdapter.getLegacyIdFromCuid(resourceId));

            if (PermissionOracle.using(em).isViewAllowed(activity.getDatabase(), em.getReference(User.class, CuidAdapter.getLegacyIdFromCuid(userId)))) {
                return true;
            }
        } else {
            throw new UnsupportedOperationException("Blob owner is not supported, ownerId: " + resourceId);
        }
        return false;
    }

    private static void assertNotAnonymousUser(@InjectParam AuthenticatedUser user) {
        if (user == null || user.isAnonymous()) {
            throw new WebApplicationException(UNAUTHORIZED);
        }
    }

    public void setTestBucketName() {
        if (bucketName == null) {
            bucketName = appIdentityService.getDefaultGcsBucketName();
        }
    }

    private void assertBlobExists(BlobId blobId) {
        try {
            // fetch just one byte of blob to make sure it exists
            BlobstoreServiceFactory.getBlobstoreService().fetchData(blobKey(blobId), 0, 1);
        } catch (Exception e) {
            throw new WebApplicationException(NOT_FOUND);
        }
    }

}
