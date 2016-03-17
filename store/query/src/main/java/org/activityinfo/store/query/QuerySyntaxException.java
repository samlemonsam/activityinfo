package org.activityinfo.store.query;

public class QuerySyntaxException extends RuntimeException {

    public QuerySyntaxException() {
    }

    public QuerySyntaxException(String message) {
        super(message);
    }

    public QuerySyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
