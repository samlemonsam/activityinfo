package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.FormHistoryEntry;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;

import java.util.List;


public class HistoryRequest implements HttpRequest<List<FormHistoryEntry>> {
    private String formId;
    private String recordId;

    public HistoryRequest(RecordRef recordRef) {
        this.formId = recordRef.getFormId().asString();
        this.recordId = recordRef.getRecordId().asString();
    }

    @Override
    public Promise<List<FormHistoryEntry>> execute(ActivityInfoClientAsync client) {
        return client.getRecordHistory(formId, recordId);
    }

    @Override
    public boolean shouldRefresh(FormChange change) {
        return false;
    }

    @Override
    public int refreshInterval(List<FormHistoryEntry> result) {
        return -1;
    }
}
