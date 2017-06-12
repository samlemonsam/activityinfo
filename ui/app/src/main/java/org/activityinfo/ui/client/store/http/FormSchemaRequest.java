package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

public class FormSchemaRequest implements HttpRequest<FormClass> {

    private ResourceId formId;

    public FormSchemaRequest(ResourceId formId) {
        this.formId = formId;
    }

    @Override
    public Promise<FormClass> execute(ActivityInfoClientAsync client) {
        return client.getFormSchema(formId.asString());
    }
}
