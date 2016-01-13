package org.activityinfo.service.blob;

import com.google.common.io.ByteSource;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;

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
     *
     * @param blobId
     * @return blob uri
     */
    @GET
    @Path("{blobId}/{resourceId}/blobUrl")
    Response getBlobUrl(@InjectParam AuthenticatedUser user,
                        @PathParam("blobId") BlobId blobId,
                        @PathParam("resourceId") ResourceId resourceId);

    /**
     * Uploads a blob with the specified id to GCS
     *
     * @param authenticatedUser
     * @param contentDisposition
     * @param mimeType
     * @param blobId
     * @param byteSource
     * @throws IOException
     */
    void put(AuthenticatedUser authenticatedUser, String contentDisposition, String mimeType, BlobId blobId,
             ResourceId resourceId,
             ByteSource byteSource) throws IOException;


    @GET
    @Path("{blobId}/{resourceId}/image")
    Response getImage(@InjectParam AuthenticatedUser user,
                      @PathParam("blobId") BlobId blobId,
                      @PathParam("resourceId") ResourceId resourceId) throws IOException;

    @GET
    @Path("{blobId}/{resourceId}/imageUrl")
    Response getImageUrl(@InjectParam AuthenticatedUser user,
                         @PathParam("blobId") BlobId blobId,
                         @PathParam("resourceId") ResourceId resourceId) throws IOException;

    @GET
    @Path("{blobId}/{resourceId}/thumbnail")
    Response getThumbnail(@InjectParam AuthenticatedUser user,
                          @PathParam("blobId") BlobId blobId,
                          @PathParam("resourceId") ResourceId resourceId,
                          @QueryParam("width") int width,
                          @QueryParam("height") int height);

    @GET
    @Path("{blobId}/{resourceId}/exists")
    Response exists(@InjectParam AuthenticatedUser user,
                    @PathParam("blobId") BlobId blobId,
                    @PathParam("resourceId") ResourceId resourceId);

    @POST
    @Path("credentials/{blobId}/{resourceId}/{fileName}")
    Response getUploadCredentials(@InjectParam AuthenticatedUser user,
                                  @PathParam("blobId") BlobId blobId,
                                  @PathParam("resourceId") ResourceId resourceId,
                                  @PathParam("fileName") String fileName);

    boolean hasAccess(ResourceId userId, BlobId blobId);
}
