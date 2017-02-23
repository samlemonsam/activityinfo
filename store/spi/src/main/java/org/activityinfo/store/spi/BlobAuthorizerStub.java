package org.activityinfo.store.spi;

/**
 * Stub BlobAuthorizer that always returns false.
 */
public class BlobAuthorizerStub implements BlobAuthorizer {
    @Override
    public boolean isOwner(int userId, String blobId) {
        return false;
    }
}
