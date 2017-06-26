package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.testing.StubScheduler;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransactionBuilder;
import org.activityinfo.observable.Connection;
import org.activityinfo.promise.Maybe;
import org.activityinfo.store.testing.*;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.offline.IDBExecutorStub;
import org.activityinfo.ui.client.store.offline.OfflineStore;
import org.activityinfo.ui.client.store.offline.SnapshotStatus;
import org.junit.Before;
import org.junit.Test;

import static org.activityinfo.observable.ObservableTesting.connect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class FormStoreTest {

    @Before
    public void setup() {
        LocaleProxy.initialize();
    }

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

        Survey survey = client.getCatalog().getSurvey();

        // We start offline
        client.setConnected(false);



        // Now the view connects and should remain in loading state...
        FormStoreImpl formStore = new FormStoreImpl(httpBus, offlineStore, scheduler);
        Connection<FormTree> view = connect(formStore.getFormTree(survey.getFormId()));
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
        Survey survey = client.getCatalog().getSurvey();

        HttpBus httpBus = new HttpBus(client, scheduler);

        Connection<FormTree> view = connect(httpBus.getFormTree(survey.getFormId()));

        runScheduled();

        view.assertLoaded();
    }


    @Test
    public void offlineRecordFetching() {
        AsyncClientStub client = new AsyncClientStub();
        HttpBus httpBus = new HttpBus(client, scheduler);
        OfflineStore offlineStore = new OfflineStore(new IDBExecutorStub());
        FormStoreImpl formStore = new FormStoreImpl(httpBus, offlineStore, scheduler);

        Survey survey = client.getCatalog().getSurvey();

        // Start online
        Connection<OfflineStatus> offlineStatusView = connect(formStore.getOfflineStatus(survey.getFormId()));

        // Initially form should not be loaded
        assertFalse(offlineStatusView.assertLoaded().isEnabled());

        // and mark the survey form for offline usage
        offlineStore.enableOffline(survey.getFormId(), true);

        assertTrue(offlineStatusView.assertLoaded().isEnabled());
        assertFalse(offlineStatusView.assertLoaded().isCached());

        // Now synchronize...
        RecordSynchronizer synchronizer = new RecordSynchronizer(httpBus, offlineStore);

        runScheduled();

        // We go offline...
        client.setConnected(false);

        // Should be able to view the form class and a record
        Connection<FormTree> schemaView = connect(formStore.getFormTree(survey.getFormId()));
        Connection<Maybe<FormRecord>> recordView = connect(formStore.getRecord(survey.getRecordRef(0)));

        runScheduled();

        schemaView.assertLoaded();
        recordView.assertLoaded();

        assertTrue(offlineStatusView.assertLoaded().isEnabled());
        assertTrue(offlineStatusView.assertLoaded().isCached());
    }

    @Test
    public void offlineColumnQuery() {

        TestSetup setup = new TestSetup();
        Survey survey = setup.getSurveyForm();

        setup.getFormStore().setFormOffline(survey.getFormId(), true);
        setup.runScheduled();
        setup.setConnected(false);


        Connection<SnapshotStatus> snapshot = setup.connect(setup.getOfflineStore().getCurrentSnapshot());
        assertTrue(snapshot.assertLoaded().isFormCached(survey.getFormId()));

        QueryModel queryModel = new QueryModel(survey.getFormId());
        queryModel.selectResourceId().as("id");
        queryModel.selectField(survey.getNameFieldId()).as("name");
        queryModel.selectField(survey.getAgeFieldId()).as("age");

        ColumnSet columnSet = setup.connect(setup.getFormStore().query(queryModel)).assertLoaded();

        assertThat(columnSet.getNumRows(), equalTo(survey.getRowCount()));
        assertThat(columnSet.getColumnView("name").get(0), equalTo("Melanie"));
        assertThat(columnSet.getColumnView("name").get(1), equalTo("Joe"));
        assertThat(columnSet.getColumnView("name").get(2), equalTo("Matilda"));
    }

    @Test
    public void relatedFormsAreAlsoCached() {
        AsyncClientStub client = new AsyncClientStub();
        IntakeForm intakeForm = client.getCatalog().getIntakeForm();

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
        assertTrue("related form is cached", snapshot.isFormCached(intakeForm.getFormId()));
    }

    @Test
    public void newRecordHitsQuery() {
        TestingCatalog catalog = new TestingCatalog();
        Survey survey = catalog.getSurvey();

        AsyncClientStub client = new AsyncClientStub(catalog);
        HttpBus httpBus = new HttpBus(client, scheduler);
        OfflineStore offlineStore = new OfflineStore(new IDBExecutorStub());
        FormStoreImpl formStore = new FormStoreImpl(httpBus, offlineStore, scheduler);

        // Open a query on a set of records

        QueryModel queryModel = new QueryModel(survey.getFormId());
        queryModel.selectResourceId().as("id");

        Connection<ColumnSet> tableView = connect(formStore.query(queryModel));
        tableView.assertLoaded();

        // Add an new record to Survey
        tableView.resetChangeCounter();
        formStore.updateRecords(new RecordTransactionBuilder().add(catalog.addNew(survey.getFormId())));


        // Verify that the table view has been updated
        tableView.assertLoaded();
        tableView.assertChanged();

        assertThat(tableView.assertLoaded().getNumRows(), equalTo(survey.getRowCount() + 1));
    }

    @Test
    public void newRecordOffline() {
        TestSetup setup = new TestSetup();
        Survey survey = setup.getSurveyForm();

        // Synchronize the survey form

        setup.setConnected(true);
        setup.getFormStore().setFormOffline(survey.getFormId(), true);
        setup.runScheduled();

        // Go offline...
        setup.setConnected(false);

        // Create a new survey record
        FormInstance newRecordTyped = survey.getGenerator().get();
        FormRecord newRecord = FormRecord.fromInstance(newRecordTyped);

        RecordTransactionBuilder tx = new RecordTransactionBuilder();


    }

    private void runScheduled() {
        while(scheduler.executeScheduledCommands()) {
            System.err.println("Still executing...");
        }
    }

}