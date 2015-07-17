package org.activityinfo.model.type;

public class ErrorValue implements FieldValue {

    private final Exception exception;

    public ErrorValue(Exception e) {
        this.exception = e;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return ErrorType.TYPE_CLASS;
    }

}
