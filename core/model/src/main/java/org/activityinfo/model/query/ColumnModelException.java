package org.activityinfo.model.query;

/**
 * Exception thrown while parsing an invalid column model
 */
public class ColumnModelException extends RuntimeException {

    public ColumnModelException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
