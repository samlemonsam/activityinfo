package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;

public class RecordRequest implements HttpRequest<FormRecord> {

    private RecordRef recordRef;

    public RecordRequest(RecordRef recordRef) {
        this.recordRef = recordRef;
    }

    @Override
    public Promise<FormRecord> execute(ActivityInfoClientAsync client) {
        return client.getRecord(recordRef.getFormId().asString(), recordRef.getRecordId().asString());
    }

    @Override
    public boolean shouldRefresh(FormChange change) {
        return false;
    }

    @Override
    public int refreshInterval(FormRecord result) {
        return -1;
    }
}
