package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.testing.StubScheduler;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.testing.TestingCatalog;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.offline.IDBExecutorStub;
import org.activityinfo.ui.client.store.offline.OfflineStore;

/**
 * Wires together a FormStoreImpl along with stubbed or mocked out components
 * suitable for unit testing.
 */
public class TestSetup {

    private final AsyncClientStub client;
    private final HttpBus httpBus;
    private final OfflineStore offlineStore;
    private final FormStore formStore;
    private final StubScheduler scheduler;
    private final TestingCatalog catalog;
    private final RecordSynchronizer synchronizer;

    public TestSetup() {
        catalog = new TestingCatalog();
        client = new AsyncClientStub(catalog);
        scheduler = new StubScheduler();
        httpBus = new HttpBus(client, scheduler);
        offlineStore = new OfflineStore(new IDBExecutorStub());
        formStore = new FormStoreImpl(httpBus, offlineStore, scheduler);
        synchronizer = new RecordSynchronizer(httpBus, offlineStore);
    }

    public FormStore getFormStore() {
        return formStore;
    }

    public OfflineStore getOfflineStore() {
        return offlineStore;
    }

    public void deleteForm(ResourceId formId) {
        catalog.deleteForm(formId);
    }

    public void runScheduled() {
        while(scheduler.executeScheduledCommands()) {
            System.err.println("Still executing...");
        }
    }

    public void setConnected(boolean connected) {
        client.setConnected(connected);
    }

    public <T> Connection<T> connect(Observable<T> observable) {

        Connection<T> connection = new Connection<>(observable);

        runScheduled();

        return connection;
    }

}
