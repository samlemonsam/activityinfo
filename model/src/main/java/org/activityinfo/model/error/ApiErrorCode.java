package org.activityinfo.model.error;

/**
 * ApiError codes for errors which can be handled programmatically,
 * or which trigger further actions on the client.
 */
public enum ApiErrorCode {

    JOB_START_FAIL("Failed to start the requested job"),
    EXPORT_COLUMN_LIMIT_REACHED("Export has too many columns for given file type"),
    DATABASE_NOT_FOUND("Database is not available."),
    EXPORT_FORMS_FORBIDDEN("User does not have rights to Export all Forms in this Database"),
    EXPORT_ACTIVITIES_FORBIDDEN("User does not have rights to Export all Activities in this Database");

    private String userMessage;

    ApiErrorCode(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}