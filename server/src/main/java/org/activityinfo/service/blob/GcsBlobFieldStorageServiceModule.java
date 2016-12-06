package org.activityinfo.service.blob;

import org.activityinfo.server.endpoint.rest.RestApiModule;

public class GcsBlobFieldStorageServiceModule extends RestApiModule {

    @Override
    protected void configureResources() {
        bind(BlobFieldStorageService.class).to(GcsBlobFieldStorageService.class);
        bind(BlobAuthorizer.class).to(GcsBlobFieldStorageService.class);
        serve("/service/appengine").with(GcsBlobServlet.class);
        bindResource(GcsBlobFieldStorageService.class);
    }
}
