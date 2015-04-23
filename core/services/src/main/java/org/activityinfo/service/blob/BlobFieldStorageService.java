package org.activityinfo.service.blob;

import com.google.common.io.ByteSource;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

/**
 * Provides storage for fields which have blob values, such as images
 * or general attachment fields.
 */
@Path("/service/blob")
public interface BlobFieldStorageService {

    /**
     * Provides a temporary, signed URL via which the user can access a blob
     * associated with a field value.
     * @param blobId
     * @return
     */
    URI getBlobUrl(BlobId blobId);

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
    @Path("{resourceId}/{fieldId}/{blobId}/image")
    public Response getImage(@InjectParam AuthenticatedUser user,
                                 @PathParam("resourceId") ResourceId resourceId,
                                 @PathParam("fieldId") ResourceId fieldId,
                                 @PathParam("blobId") BlobId blobId) throws IOException;

    @GET
    @Path("{resourceId}/{fieldId}/{blobId}/thumbnail")
    public Response getThumbnail(@InjectParam AuthenticatedUser user,
                                 @PathParam("resourceId") ResourceId resourceId,
                                 @PathParam("fieldId") ResourceId fieldId,
                                 @PathParam("blobId") BlobId blobId,
                                 @QueryParam("width") int width,
                                 @QueryParam("height") int height);

    @POST
    @Path("credentials/{blobId}")
    Response getUploadCredentials(@InjectParam AuthenticatedUser user,
                                  @PathParam("blobId") BlobId blobId);
}
