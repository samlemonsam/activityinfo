package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.FormRecordSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;

/**
 * Requests a range of record versions for a given form.
 */
public class VersionRangeRequest implements HttpRequest<FormRecordSet> {

    private ResourceId formId;
    private long localVersion;
    private long version;

    public VersionRangeRequest(ResourceId formId, long localVersion, long version) {
        this.formId = formId;
        this.localVersion = localVersion;
        this.version = version;
    }

    @Override
    public Promise<FormRecordSet> execute(ActivityInfoClientAsync client) {
        return client.getRecordVersionRange(formId.asString(), localVersion, version);
    }

    @Override
    public boolean shouldRefresh(FormChange change) {
        return false;
    }

    @Override
    public int refreshInterval(FormRecordSet result) {
        return -1;
    }
}
