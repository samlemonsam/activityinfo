package org.activityinfo.service.blob;

import com.google.common.io.ByteSource;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;

public class TestBlobFieldStorageService implements BlobFieldStorageService {
    @Override
    public URI getBlobUrl(BlobId blobId) {
        return null;
    }

    @Override
    public void put(AuthenticatedUser authenticatedUser, String contentDisposition, String mimeType, BlobId blobId,
                    ByteSource byteSource) {
    }

    @Override
    public Response getImage(@InjectParam AuthenticatedUser user,
                             @PathParam("resourceId") ResourceId resourceId,
                             @PathParam("fieldId") ResourceId fieldId,
                             @PathParam("blobId") BlobId blobId) {
        throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
    }

    @Override
    public Response getThumbnail(@InjectParam AuthenticatedUser user,
                                 @PathParam("resourceId") ResourceId resourceId,
                                 @PathParam("fieldId") ResourceId fieldId,
                                 @PathParam("blobId") BlobId blobId,
                                 @QueryParam("width") int width,
                                 @QueryParam("height") int height) {
        throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
    }

    @Override
    public Response getUploadCredentials(@InjectParam AuthenticatedUser user, BlobId blobId) {
        throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
    }
}
