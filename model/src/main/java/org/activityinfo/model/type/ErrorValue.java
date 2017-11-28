package org.activityinfo.model.type;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

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
    public JsonValue toJsonElement() {
        return Json.createNull();
    }


    @Override
    public String toString() {
        return "ErrorValue{" + exception.getMessage() + "}";
    }
}
