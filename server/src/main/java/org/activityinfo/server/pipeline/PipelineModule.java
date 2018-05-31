package org.activityinfo.server.pipeline;

import org.activityinfo.server.endpoint.rest.RestApiModule;

public class PipelineModule extends RestApiModule {

    @Override
    protected void configureResources() {
        bindResource(PipelineResource.class);
    }

}
