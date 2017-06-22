package org.activityinfo.store.query.shared;

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
