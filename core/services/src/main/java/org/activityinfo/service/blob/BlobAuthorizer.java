package org.activityinfo.service.blob;

/**
 * Determines whether a user owns a lbob
 */
public interface BlobAuthorizer {

    /**
     * Returns true if the given user owns the given blobId
     */
    boolean isOwner(int userId, String blobId);
}
