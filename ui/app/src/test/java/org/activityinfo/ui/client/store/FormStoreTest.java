package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.testing.StubScheduler;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.ObservableTesting;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.offline.IDBExecutorStub;
import org.activityinfo.ui.client.store.offline.OfflineStore;
import org.junit.Test;

public class FormStoreTest {


    private final ImmediateScheduler scheduler = new ImmediateScheduler();

    /**
     * If at first the remote fetch does not succeed because
     * of network problems, we should keep retrying.
     */
    @Test
    public void formSchemaFetchesAreRetried() {

        AsyncClientStub client = new AsyncClientStub();
        StubScheduler retryScheduler = new StubScheduler();
        HttpBus httpBus = new HttpBus(client, retryScheduler);
        OfflineStore offlineStore = new OfflineStore(new IDBExecutorStub());

        // We start offline
        client.setConnected(false);

        // Now the view connects and should remain in loading state...
        FormStoreImpl formStore = new FormStoreImpl(httpBus, offlineStore, scheduler);
        Connection<FormTree> view = ObservableTesting.connect(formStore.getFormTree(Survey.FORM_ID));
        view.assertLoading();

        // Start retries, but we're still offline
        retryScheduler.executeRepeatingCommands();
        view.assertLoading();

        // Now connect and retry
        client.setConnected(true);
        retryScheduler.executeRepeatingCommands();

        // View should be loaded
        view.assertLoaded();
    }


    @Test
    public void offlineRecordFetching() {
        AsyncClientStub client = new AsyncClientStub();
        HttpBus httpBus = new HttpBus(client, new StubScheduler());
        OfflineStore offlineStore = new OfflineStore(new IDBExecutorStub());
        FormStoreImpl formStore = new FormStoreImpl(httpBus, offlineStore, scheduler);

        // Start online
        // and mark the survey form for offline usage
        offlineStore.enableOffline(Survey.FORM_ID, true);

        // Now synchronize...
        RecordSynchronizer synchronizer = new RecordSynchronizer(httpBus, offlineStore);
        scheduler.executeCommands();


        // We go offline...
        client.setConnected(false);

        // Should be able to view the form class and a record
        Connection<FormTree> schemaView = ObservableTesting.connect(formStore.getFormTree(Survey.FORM_ID));
        Connection<FormRecord> recordView = ObservableTesting.connect(formStore.getRecord(Survey.getRecordRef(0)));

        schemaView.assertLoaded();
        recordView.assertLoaded();
    }



}