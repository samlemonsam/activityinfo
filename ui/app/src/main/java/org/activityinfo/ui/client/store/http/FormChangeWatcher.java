package org.activityinfo.ui.client.store.http;

import com.google.common.base.Predicate;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import org.activityinfo.ui.client.store.FormChange;
import org.activityinfo.ui.client.store.FormChangeEvent;
import org.activityinfo.ui.client.store.tasks.Watcher;


public class FormChangeWatcher implements Watcher {

    private final EventBus eventBus;

    private HandlerRegistration registration;

    private Predicate<FormChange> predicate;


    public FormChangeWatcher(EventBus eventBus, Predicate<FormChange> predicate) {
        this.eventBus = eventBus;
        this.predicate = predicate;
    }


    @Override
    public void start(RefetchHandler handler) {
        registration = eventBus.addHandler(FormChangeEvent.TYPE, event -> {
            if (predicate.test(event.getChange())) {
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
