package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.testing.StubScheduler;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.ObservableTesting;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.http.HttpBus;
import org.junit.Ignore;
import org.junit.Test;

public class FormStoreTest {


    /**
     * If at first the remote fetch does not succeed because
     * of network problems, we should keep retrying.
     */
    @Test
    public void formSchemaFetchesAreRetried() {

        AsyncClientStub client = new AsyncClientStub();
        StubScheduler retryScheduler = new StubScheduler();
        HttpBus httpBus = new HttpBus(client, retryScheduler);
        OfflineStoreStub offlineStore = new OfflineStoreStub();

        // We start offline
        client.setConnected(false);

        // Now the view connects and should remain in loading state...
        FormStoreImpl formStore = new FormStoreImpl(httpBus, offlineStore);
        Connection<FormClass> view = ObservableTesting.connect(formStore.getFormClass(Survey.FORM_ID));
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


    /**
     * Once fetched from the server, form schemas should be cached
     * in IndexedDb and should be available from the offline store.
     */
    @Test
    public void formSchemasAreCachedOffline() {

        AsyncClientStub client = new AsyncClientStub();
        HttpBus httpBus = new HttpBus(client, new StubScheduler());
        OfflineStoreStub offlineStore = new OfflineStoreStub();

        // So we start online.
        // But nothing happens until a UI view starts observing a form class
        FormStoreImpl formStore = new FormStoreImpl(httpBus, offlineStore);
        Connection<FormClass> view = ObservableTesting.connect(formStore.getFormClass(Survey.FORM_ID));
        view.assertLoaded();

        // Then we navigate away and the view releases the connection
        // to the form class.
        view.disconnect();

        // Now we go offline...
        client.setConnected(false);

        // And start a new session
        FormStoreImpl formStoreOffline = new FormStoreImpl(httpBus, offlineStore);

        // So when the form schema is needed again then verify that
        // it can be loaded from the offline store
        view = ObservableTesting.connect(formStoreOffline.getFormClass(Survey.FORM_ID));
        view.assertLoaded();

    }


    @Ignore
    @Test
    public void offlineRecordFetching() {
        AsyncClientStub client = new AsyncClientStub();
        HttpBus httpBus = new HttpBus(client, new StubScheduler());
        OfflineStoreStub offlineStore = new OfflineStoreStub();
        FormStoreImpl formStore = new FormStoreImpl(httpBus, offlineStore);

        // Start online
        // and mark the survey form for offline usage
        formStore.enableFormOffline(Survey.FORM_ID, true);

        // Now synchronize...
        // (TODO)

        // We go offline...
        client.setConnected(false);

        // Should be able to view the form class and a record
        Connection<FormClass> schemaView = ObservableTesting.connect(formStore.getFormClass(Survey.FORM_ID));
        Connection<FormRecord> recordView = ObservableTesting.connect(formStore.getRecord(Survey.getRecordRef(0)));

        schemaView.assertLoaded();
        recordView.assertLoaded();



    }



}