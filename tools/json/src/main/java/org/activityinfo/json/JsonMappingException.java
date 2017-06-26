package org.activityinfo.json;

/**
 * Thrown when a JSON value could not be matched to the expected type.
 */
public class JsonMappingException extends Exception {


    public JsonMappingException() {
        super();
    }

    public JsonMappingException(String message) {
        super(message);
    }


    public JsonMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
