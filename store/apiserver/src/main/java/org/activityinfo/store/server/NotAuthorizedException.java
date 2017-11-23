package org.activityinfo.store.server;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class NotAuthorizedException extends WebApplicationException {

    public NotAuthorizedException() {
        super(Response.Status.FORBIDDEN);
    }
}
