package org.activityinfo.ui.client.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.promise.Promise;


public interface HttpRequest<T> {

    Promise<T> execute(ActivityInfoClientAsync async);

}
