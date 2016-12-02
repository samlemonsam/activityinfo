package org.activityinfo.core.shared.importing.schema;

class UnableToParseRowException extends RuntimeException {

    public UnableToParseRowException() {
    }

    UnableToParseRowException(String message) {
        super(message);
    }

}
