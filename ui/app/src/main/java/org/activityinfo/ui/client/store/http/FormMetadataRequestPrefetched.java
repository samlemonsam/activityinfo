package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

class FormMetadataRequestPrefetched implements HttpRequest<FormMetadata> {

    private final ResourceId formId;
    private FormMetadata prefetched;

    public FormMetadataRequestPrefetched(FormMetadata prefetched) {
        this.formId = prefetched.getId();
        this.prefetched = prefetched;
    }

    @Override
    public Promise<FormMetadata> execute(ActivityInfoClientAsync async) {
        if(isInitialRequest()) {
            Promise<FormMetadata> result = Promise.resolved(prefetched);
            prefetched = null;
            return result;
        }

        // Otherwise, if this is a refresh, query only this form individually
        return async.getFormMetadata(formId.asString());
    }

    private boolean isInitialRequest() {
        return prefetched != null;
    }

    @Override
    public int refreshInterval(FormMetadata result) {
        return -1;
    }
}
