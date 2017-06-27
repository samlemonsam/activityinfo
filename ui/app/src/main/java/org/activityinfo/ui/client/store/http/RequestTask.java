package org.activityinfo.ui.client.store.http;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.ui.client.store.tasks.Task;
import org.activityinfo.ui.client.store.tasks.TaskExecution;

class RequestTask<T> implements Task<T> {

    private HttpBus bus;
    private HttpRequest<T> request;

    public RequestTask(HttpBus bus, HttpRequest<T> request) {
        this.bus = bus;
        this.request = request;
    }

    @Override
    public TaskExecution start(AsyncCallback<T> callback) {
        return bus.submit(request, callback);
    }

    @Override
    public int refreshInterval(T result) {
        return request.refreshInterval(result);
    }
}
