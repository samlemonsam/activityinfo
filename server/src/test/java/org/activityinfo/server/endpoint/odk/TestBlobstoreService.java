package org.activityinfo.server.endpoint.odk;

import com.google.common.io.ByteSource;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.blob.BlobFieldStorageService;
import org.activityinfo.service.blob.BlobId;

import javax.ws.rs.core.Response;
import java.io.IOException;

public class TestBlobstoreService implements BlobFieldStorageService {

    @Override
    public Response getBlobUrl(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(AuthenticatedUser user, String contentDisposition, String mimeType,
                    BlobId blobId, ResourceId resourceId, ByteSource byteSource) throws IOException {

    }

    @Override
    public Response getImage(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response getImageUrl(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response getThumbnail(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId, int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response exists(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response getUploadCredentials(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId, String fileName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAccess(ResourceId userId, BlobId blobId) {
        return false;
    }
}
