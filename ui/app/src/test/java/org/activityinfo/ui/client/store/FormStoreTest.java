package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.testing.StubScheduler;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.observable.Connection;
import org.activityinfo.store.testing.IncidentForm;
import org.activityinfo.store.testing.IntakeForm;
import org.activityinfo.store.testing.ReferralSubForm;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.offline.IDBExecutorStub;
import org.activityinfo.ui.client.store.offline.OfflineStore;
import org.activityinfo.ui.client.store.offline.SnapshotStatus;
import org.junit.Test;

import static org.activityinfo.observable.ObservableTesting.connect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FormStoreTest {


    private final StubScheduler scheduler = new StubScheduler();

    /**
     * If at first the remote fetch does not succeed because
     * of network problems, we should keep retrying.
     */
    @Test
    public void formSchemaFetchesAreRetried() {

        AsyncClientStub client = new AsyncClientStub();
        HttpBus httpBus = new HttpBus(client, scheduler);
        OfflineStore offlineStore = new OfflineStore(new IDBExecutorStub());

        // We start offline
        client.setConnected(false);


        // Now the view connects and should remain in loading state...
        FormStoreImpl formStore = new FormStoreImpl(httpBus, offlineStore, scheduler);
        Connection<FormTree> view = connect(formStore.getFormTree(Survey.FORM_ID));
        view.assertLoading();

        // Start retries, but we're still offline
        scheduler.executeCommands();

        view.assertLoading();

        // Now connect and retry
        client.setConnected(true);

        scheduler.executeCommands();

        // View should be loaded
        view.assertLoaded();
    }


    @Test
    public void httpBus() {
        AsyncClientStub client = new AsyncClientStub();
        HttpBus httpBus = new HttpBus(client, scheduler);

        Connection<FormTree> view = connect(httpBus.getFormTree(Survey.FORM_ID));

        runScheduled();

        view.assertLoaded();
    }


    @Test
    public void offlineRecordFetching() {
        AsyncClientStub client = new AsyncClientStub();
        HttpBus httpBus = new HttpBus(client, scheduler);
        OfflineStore offlineStore = new OfflineStore(new IDBExecutorStub());
        FormStoreImpl formStore = new FormStoreImpl(httpBus, offlineStore, scheduler);

        // Start online
        Connection<OfflineStatus> offlineStatusView = connect(formStore.getOfflineStatus(Survey.FORM_ID));

        // Initially form should not be loaded
        assertFalse(offlineStatusView.assertLoaded().isEnabled());

        // and mark the survey form for offline usage
        offlineStore.enableOffline(Survey.FORM_ID, true);

        assertTrue(offlineStatusView.assertLoaded().isEnabled());
        assertFalse(offlineStatusView.assertLoaded().isCached());

        // Now synchronize...
        RecordSynchronizer synchronizer = new RecordSynchronizer(httpBus, offlineStore);

        runScheduled();

        // We go offline...
        client.setConnected(false);

        // Should be able to view the form class and a record
        Connection<FormTree> schemaView = connect(formStore.getFormTree(Survey.FORM_ID));
        Connection<FormRecord> recordView = connect(formStore.getRecord(Survey.getRecordRef(0)));

        runScheduled();

        schemaView.assertLoaded();
        recordView.assertLoaded();

        assertTrue(offlineStatusView.assertLoaded().isEnabled());
        assertTrue(offlineStatusView.assertLoaded().isCached());
    }

    @Test
    public void relatedFormsAreAlsoCached() {
        AsyncClientStub client = new AsyncClientStub();
        HttpBus httpBus = new HttpBus(client, scheduler);
        OfflineStore offlineStore = new OfflineStore(new IDBExecutorStub());
        FormStoreImpl formStore = new FormStoreImpl(httpBus, offlineStore, scheduler);

        // Start online, and enable offline mode for incidents
        formStore.setFormOffline(IncidentForm.FORM_ID, true);

        // Now synchronize...
        RecordSynchronizer synchronizer = new RecordSynchronizer(httpBus, offlineStore);

        runScheduled();

        // Ensure that related forms and subforms are also synchronized
        SnapshotStatus snapshot = connect(offlineStore.getCurrentSnapshot()).assertLoaded();

        assertTrue("incident form is cached", snapshot.isFormCached(IncidentForm.FORM_ID));
        assertTrue("sub form is cached", snapshot.isFormCached(ReferralSubForm.FORM_ID));
        assertTrue("related form is cached", snapshot.isFormCached(IntakeForm.FORM_ID));
    }


    private void runScheduled() {
        while(scheduler.executeScheduledCommands()) {
            System.err.println("Still executing...");
        }
    }

}