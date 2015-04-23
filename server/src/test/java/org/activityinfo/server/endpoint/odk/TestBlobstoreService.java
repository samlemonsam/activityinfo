package org.activityinfo.server.endpoint.odk;

import com.google.common.io.ByteSource;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.blob.BlobFieldStorageService;
import org.activityinfo.service.blob.BlobId;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

public class TestBlobstoreService implements BlobFieldStorageService {
    @Override
    public URI getBlobUrl(BlobId blobId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(AuthenticatedUser authenticatedUser, String contentDisposition, String mimeType, BlobId blobId, ByteSource byteSource) throws IOException {

    }

    @Override
    public Response getImage(@InjectParam AuthenticatedUser user, ResourceId resourceId, ResourceId fieldId, BlobId blobId) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response getThumbnail(@InjectParam AuthenticatedUser user, ResourceId resourceId, ResourceId fieldId, BlobId blobId, int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response getUploadCredentials(@InjectParam AuthenticatedUser user, BlobId blobId) {
        throw new UnsupportedOperationException();
    }
}
