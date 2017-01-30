package org.activityinfo.legacy.shared.impl.pivot;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface WorkItem {

    public void execute(AsyncCallback<Void> callback);
}
