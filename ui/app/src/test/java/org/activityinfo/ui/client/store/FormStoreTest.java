package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.testing.StubScheduler;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.ObservableTesting;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.http.HttpBus;
import org.junit.Test;

public class FormStoreTest {


    /**
     * Once fetched from the server, form schemas should be cached
     * in IndexedDb and should be available from
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

}