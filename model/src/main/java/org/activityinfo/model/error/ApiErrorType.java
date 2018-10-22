package org.activityinfo.model.error;

/**
 * The type of error experienced during an ActivityInfo API call, and its corresponding HTTP Error Code
 */
public enum ApiErrorType {

    /**
     * Request was malformed or had invalid parameters. Uses HTTP Response Code 400 (Bad Request)
     */
    INVALID_REQUEST_ERROR(400),

    /**
     * User could not be authenticated. Uses HTTP Response Code 401 (Unauthorized)
     */
    AUTHENTICATION_ERROR(401),

    /**
     * User was not permitted to issue request due to billing restrictions. Uses HTTP Response Code 402
     */
    BILLING_ERROR(402),

    /**
     * User was not permitted to issue request due to permission restrictions. Uses HTTP Response Code 403 (Forbidden)
     */
    AUTHORIZATION_ERROR(403),

    /**
     * Request was valid, but the data contained in request was invalid. Uses HTTP Response Code 409 (Conflict)
     */
    VALIDATION_ERROR(409),

    /**
     * Server-side error occurred while processing request. Uses HTTP Response Code 500 (Internal Server Error)
     */
    SERVER_ERROR(500);

    private int httpResponseCode;

    ApiErrorType(final int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }
}