package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;

public class FormMetadataRequest implements HttpRequest<FormMetadata> {

    private ResourceId formId;

    public FormMetadataRequest(ResourceId formId) {
        this.formId = formId;
    }

    @Override
    public Promise<FormMetadata> execute(ActivityInfoClientAsync async) {
        return async.getFormMetadata(formId.asString());
    }


    @Override
    public int refreshInterval(FormMetadata result) {
        return -1;
    }
}
