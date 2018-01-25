package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.form.RecordHistory;
import org.activityinfo.model.form.RecordHistoryEntry;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;

import java.util.List;


public class HistoryRequest implements HttpRequest<RecordHistory> {
    private String formId;
    private String recordId;

    public HistoryRequest(RecordRef recordRef) {
        this.formId = recordRef.getFormId().asString();
        this.recordId = recordRef.getRecordId().asString();
    }

    @Override
    public Promise<RecordHistory> execute(ActivityInfoClientAsync client) {
        return client.getRecordHistory(formId, recordId);
    }

    @Override
    public int refreshInterval(RecordHistory result) {
        return -1;
    }
}
