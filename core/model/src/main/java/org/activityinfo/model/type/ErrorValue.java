package org.activityinfo.model.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

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

    @Override
    public JsonElement toJsonElement() {
        return JsonNull.INSTANCE;
    }


    @Override
    public String toString() {
        return "ErrorValue{" + exception.getMessage() + "}";
    }
}
