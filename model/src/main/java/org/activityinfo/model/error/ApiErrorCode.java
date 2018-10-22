package org.activityinfo.model.error;

/**
 * ApiError codes for errors which can be handled programmatically,
 * or which trigger further actions on the client.
 */
public enum ApiErrorCode {

    /**
     * Failed to start the requested job
     */
    JOB_START_FAIL,

    /**
     * Export has too many columns for given file type
     */
    EXPORT_COLUMN_LIMIT_REACHED

}