package org.activityinfo.api.client;

/**
 * Exception thrown by the server
 */
public class ApiException extends RuntimeException {
    
    public ApiException(int statusCode) {
        super(Integer.toString(statusCode));
    }

    public ApiException(Throwable cause) {
        super(cause);
    }
}
