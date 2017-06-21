package org.activityinfo.ui.client.store.http;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import org.activityinfo.ui.client.store.FormChangeEvent;


public class HttpWatcher implements Watcher {

    private final EventBus eventBus;
    private final HttpRequest<?> request;

    private HandlerRegistration registration;

    public HttpWatcher(EventBus eventBus, HttpRequest<?> request) {
        this.eventBus = eventBus;
        this.request = request;
    }


    @Override
    public void start(RefetchHandler handler) {
        registration = eventBus.addHandler(FormChangeEvent.TYPE, event -> {
            if (request.shouldRefresh(event.getPredicate())) {
                // Our current result has become outdated, we need to fetch a new version from
                // the server
                handler.refetch();
            }
        });
    }

    @Override
    public void stop() {
        assert registration != null : "Watcher not started!";

        registration.removeHandler();
        registration = null;
    }
}
