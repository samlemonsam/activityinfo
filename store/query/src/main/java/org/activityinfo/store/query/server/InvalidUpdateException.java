package org.activityinfo.store.query.server;


public class InvalidUpdateException extends RuntimeException {

    public InvalidUpdateException(String message) {
        super(message);
    }

    public InvalidUpdateException(String message, Object... args) {
        super(String.format(message, args));
    }


    public InvalidUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidUpdateException(Throwable cause) {
        super(cause);
    }

}
