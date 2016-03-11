package org.activityinfo.i18n.tools.model;

/**
 * Thrown when encountering an ill-formed message string
 */
public class MessageFormatException extends Exception {

    public MessageFormatException(String message) {
        super(message);
    }

    public MessageFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
