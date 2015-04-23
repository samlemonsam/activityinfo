package org.activityinfo.server.endpoint.odk;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class OpenRosaResponse {


    /**
     * ODK will only parse the formList as an OpenRosa form list if the r
     * response includes this header.
     */
    private static final String OPEN_ROSA_VERSION_HEADER = "X-OpenRosa-Version";

    private static final String OPEN_ROSA_VERSION = "1.0";

    public static Response build(Object entity) {
        return Response.ok(entity)
                .header(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION)
                .type(MediaType.TEXT_XML)
                .build();
    }
}
