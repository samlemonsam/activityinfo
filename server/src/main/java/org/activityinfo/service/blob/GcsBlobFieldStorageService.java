package org.activityinfo.service.blob;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFileOptions.Builder;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.server.DeploymentEnvironment;
import org.activityinfo.server.util.blob.DevAppIdentityService;
import org.activityinfo.service.DeploymentConfiguration;
import org.activityinfo.service.gcs.GcsAppIdentityServiceUrlSigner;
import org.joda.time.Period;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path("/service/blob")
public class GcsBlobFieldStorageService implements BlobFieldStorageService {
    private static final int ONE_MEGABYTE = 1 << 20;
    private static final Logger LOGGER = Logger.getLogger(GcsBlobFieldStorageService.class.getName());

    private final String bucketName;
    private AppIdentityService appIdentityService;

    @Inject
    public GcsBlobFieldStorageService(DeploymentConfiguration config) {
        this.bucketName = config.getBlobServiceBucketName();
        appIdentityService = DeploymentEnvironment.isAppEngineDevelopment() ?
                new DevAppIdentityService(config) : AppIdentityServiceFactory.getAppIdentityService();

        LOGGER.info("Service account: " + appIdentityService.getServiceAccountName());
    }

    @Override
    public URI getBlobUrl(BlobId blobId) {
        GcsAppIdentityServiceUrlSigner signer = new GcsAppIdentityServiceUrlSigner();
        try {
            return new URI(signer.getSignedUrl("GET", bucketName + "/" + blobId.asString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void put(AuthenticatedUser authenticatedUser, String contentDisposition, String mimeType, BlobId blobId,
                    ByteSource byteSource) throws IOException {
        GcsFilename gcsFilename = new GcsFilename(bucketName, blobId.asString());
        GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        Builder builder = new Builder();

        builder.contentDisposition(contentDisposition);
        builder.mimeType(mimeType);

        GcsFileOptions gcsFileOptions = builder.build();
        GcsOutputChannel channel = gcsService.createOrReplace(gcsFilename, gcsFileOptions);

        try (OutputStream outputStream = Channels.newOutputStream(channel)) {
            byteSource.copyTo(outputStream);
        }
    }

    @GET
    @Path("{resourceId}/{fieldId}/{blobId}/image")
    @Override
    public Response getImage(@InjectParam AuthenticatedUser user,
                             @PathParam("resourceId") ResourceId resourceId,
                             @PathParam("fieldId") ResourceId fieldId,
                             @PathParam("blobId") BlobId blobId) throws IOException {
        /* TODO: Ensure that users can download images they've just uploaded
        ImageRowValue imageRowValue = getImageRowValue(user, resourceId, fieldId, blobId);
        */
        GcsFilename gcsFilename = new GcsFilename(bucketName, blobId.asString());
        GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        GcsInputChannel gcsInputChannel = gcsService.openPrefetchingReadChannel(gcsFilename, 0, ONE_MEGABYTE);

        try (InputStream inputStream = Channels.newInputStream(gcsInputChannel)) {
            return Response.ok(ByteStreams.toByteArray(inputStream))/*.type(imageRowValue.getMimeType())*/.build();
        }
    }

    @GET
    @Path("{resourceId}/{fieldId}/{blobId}/thumbnail")
    @Override
    public Response getThumbnail(@InjectParam AuthenticatedUser user,
                                 @PathParam("resourceId") ResourceId resourceId,
                                 @PathParam("fieldId") ResourceId fieldId,
                                 @PathParam("blobId") BlobId blobId,
                                 @QueryParam("width") int width,
                                 @QueryParam("height") int height) {
        /* TODO: Ensure that users can see thumbnails of images they've just uploaded
        ImageRowValue imageRowValue = getImageRowValue(user, resourceId, fieldId, blobId);
        */

        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

        BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucketName + "/" + blobId.asString());

        Image image = ImagesServiceFactory.makeImageFromBlob(blobKey);

        /*
        if (width != imageRowValue.getWidth() || imageRowValue.getHeight() != height) {
        */
            Transform resize = ImagesServiceFactory.makeResize(width, height);
            Image newImage = imagesService.applyTransform(resize, image);

            byte[] imageData = newImage.getImageData();
            return Response.ok(imageData)/*.type(imageRowValue.getMimeType())*/.build();
        /*
        } else {
            return Response.ok(image.getImageData()).type(imageRowValue.getMimeType()).build();
        }
        */
    }

    @POST
    @Path("credentials/{blobId}")
    @Override
    public Response getUploadCredentials(@InjectParam AuthenticatedUser user,
                                         @PathParam("blobId") BlobId blobId) {
        if (user == null || user.isAnonymous()) throw new WebApplicationException(UNAUTHORIZED);

        GcsUploadCredentialBuilder builder = new GcsUploadCredentialBuilder(appIdentityService);
        builder.setBucket(bucketName);
        builder.setKey(blobId.asString());
        builder.setMaxContentLengthInMegabytes(10);
        builder.expireAfter(Period.minutes(5));
        return Response.ok(Resources.toJsonObject(builder.build().asRecord())).build();
    }


}
