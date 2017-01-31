package org.activityinfo.legacy.shared.impl.pivot;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.Log;

public class ErrorLoggingWorkItem implements WorkItem {

    private WorkItem item;

    public ErrorLoggingWorkItem(WorkItem item) {
        this.item = item;
    }

    @Override
    public void execute(final AsyncCallback<Void> callback) {
        item.execute(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Log.error("Calculated indicator query failed", caught);
                callback.onSuccess(null);
            }

            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(null);
            }
        });
    }
}
