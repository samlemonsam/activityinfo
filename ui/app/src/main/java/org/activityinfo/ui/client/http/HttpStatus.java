package org.activityinfo.ui.client.http;

/**
 * The current state of the connection with the server.
 */
public enum HttpStatus {
    IDLE,
    FETCHING,
    BROKEN
}
