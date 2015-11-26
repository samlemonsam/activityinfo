package org.activityinfo.service.blob;

import com.google.common.io.ByteSource;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.model.auth.AuthenticatedUser;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

/**
 * Provides storage for fields which have blob values, such as images
 * or general attachment fields.
 */

//@Path("/service/blob") commented path to avoid jersey conflict during binding in GcsBlobFieldStorageServiceModule
//Error : Conflicting URI templates. The URI template /service/blob for root resource class org.activityinfo.service.blob.GcsBlobFieldStorageService and the URI template /service/blob transform to the same regular expression /service/blob(/.*)?
public interface BlobFieldStorageService {

    /**
     * Provides a temporary, signed URL via which the user can access a blob
     * associated with a field value.
     * @param blobId
     * @return
     */
    @GET
    @Path("{blobId}/blob_url")
    Response getBlobUrl(@PathParam("blobId") BlobId blobId);

    /**
     * Uploads a blob with the specified id to GCS
     * @param authenticatedUser
     * @param contentDisposition
     * @param mimeType
     * @param blobId
     * @param byteSource
     * @throws IOException
     */
    void put(AuthenticatedUser authenticatedUser, String contentDisposition, String mimeType, BlobId blobId,
             ByteSource byteSource) throws IOException;


    @GET
    @Path("{blobId}/image")
    Response getImage(@InjectParam AuthenticatedUser user,
                             @PathParam("blobId") BlobId blobId) throws IOException;

    @GET
    @Path("{blobId}/image_url")
    Response getImageUrl(@InjectParam AuthenticatedUser user,
                                @PathParam("blobId") BlobId blobId) throws IOException;

    @GET
    @Path("{blobId}/thumbnail")
    Response getThumbnail(@InjectParam AuthenticatedUser user,
                                 @PathParam("blobId") BlobId blobId,
                                 @QueryParam("width") int width,
                                 @QueryParam("height") int height);

    @POST
    @Path("credentials/{blobId}/{fileName}")
    Response getUploadCredentials(@InjectParam AuthenticatedUser user,
                                  @PathParam("blobId") BlobId blobId,
                                  @PathParam("fileName") String fileName);
}
