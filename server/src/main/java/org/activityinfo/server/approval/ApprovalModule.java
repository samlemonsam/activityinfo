package org.activityinfo.server.approval;

import org.activityinfo.server.endpoint.rest.RestApiModule;

public class ApprovalModule extends RestApiModule {

    @Override
    protected void configureResources() {
        bindResource(ApprovalResource.class);
    }

}
