package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;

import java.util.List;

public class SubRecordsRequest implements HttpRequest<List<FormRecord>> {
    private String formId;
    private String parentId;

    public SubRecordsRequest(ResourceId formId, RecordRef parent) {
        this.formId = formId.asString();
        this.parentId = parent.getRecordId().asString();
    }

    @Override
    public Promise<List<FormRecord>> execute(ActivityInfoClientAsync client) {
        return client.getRecords(formId, parentId).then(set -> set.getRecords());
    }

    @Override
    public int refreshInterval(List<FormRecord> result) {
        return 0;
    }
}
