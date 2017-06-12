package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

import java.util.List;

public class CatalogRequest implements HttpRequest<List<CatalogEntry>> {

    private String parentId;

    public CatalogRequest() {
        this.parentId = null;
    }

    public CatalogRequest(String parentId) {
        this.parentId = parentId;
    }

    public CatalogRequest(ResourceId parentId) {
        this.parentId = parentId.asString();
    }

    @Override
    public Promise<List<CatalogEntry>> execute(ActivityInfoClientAsync client) {
        return client.getFormCatalog(parentId);
    }
}
