package org.activityinfo.ui.client.store.offline;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import junit.framework.TestCase;
import org.activityinfo.api.client.FormRecordSet;
import org.activityinfo.model.form.*;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.store.FormStoreImpl;
import org.activityinfo.ui.client.store.http.HttpBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tests the compiled Javascript offline store against a real browser.
 *
 * Because of the cost/time of setting up the database, we run only a single test with
 * lots of interaction.
 *
 */
public class OfflineStoreGwtTest extends GWTTestCase {


    private OfflineStore offlineStore;
    private HttpBus httpBus;
    private FormStoreImpl formStore;
    private Survey survey;

    @Override
    public String getModuleName() {
        return "org.activityinfo.ui.AppTest";
    }

    public void testStoreForm() {

        survey = new Survey();

        FormMetadata surveyMetadata = new FormMetadata();
        surveyMetadata.setId(Survey.FORM_ID);
        surveyMetadata.setVersion(1);
        surveyMetadata.setSchemaVersion(1);
        surveyMetadata.setSchema(survey.getFormClass());

        // First put the schema to the store

        Snapshot snapshot = new Snapshot(
                Collections.singletonList(surveyMetadata),
                Collections.singletonList(toFormRecordSet(survey)));

        offlineStore = new OfflineStore(new IDBExecutorImpl());
        httpBus = new HttpBus(new OfflineClientStub());
        formStore = new FormStoreImpl(httpBus, offlineStore, Scheduler.get());

        offlineStore.store(snapshot).then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                fail();
            }

            @Override
            public void onSuccess(Void result) {

                // Once the store is complete,
                // verify that the Form Schema can be read...

                verifyWeCanReadFormSchemas()
                        .join(OfflineStoreGwtTest.this::verifyWeCanQueryRecords)
                        .then(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        fail();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        finishTest();
                    }
                });

            }
        });

        delayTestFinish(5000);
    }

    private Promise<Void> verifyWeCanReadFormSchemas() {

        return formStore.getFormMetadata(Survey.FORM_ID).once().then(formMetadata -> {
            FormClass result = formMetadata.getSchema();
            TestCase.assertEquals(Survey.FORM_ID, result.getId());
            TestCase.assertEquals(survey.getFormClass().getLabel(), result.getLabel());

            FormField expectedField = survey.getFormClass().getField(Survey.GENDER_FIELD_ID);
            FormField field = result.getField(Survey.GENDER_FIELD_ID);

            TestCase.assertEquals(expectedField.getId(), field.getId());
            TestCase.assertEquals(expectedField.getLabel(), field.getLabel());
            return null;
        });
    }

    private Promise<Void> verifyWeCanQueryRecords(Void input) {

        QueryModel queryModel = new QueryModel(Survey.FORM_ID);
        queryModel.selectResourceId().as("id");
        queryModel.selectField(Survey.NAME_FIELD_ID).as("name");
        queryModel.selectField(Survey.AGE_FIELD_ID).as("age");

        return formStore.query(queryModel).once().then(columnSet -> {

            assertEquals(Survey.ROW_COUNT, columnSet.getNumRows());

            ColumnView name = columnSet.getColumnView("name");
            ColumnView age = columnSet.getColumnView("age");

            assertEquals(Survey.ROW_COUNT, columnSet.getNumRows());
            assertEquals("Melanie", name.get(0));
            assertEquals("Joe", name.get(1));
            assertEquals("Matilda", name.get(2));

            return null;
        });

    }

    private FormRecordSet toFormRecordSet(Survey survey) {

        List<FormRecord> records = new ArrayList<>();
        for (FormInstance instance : survey.getRecords()) {
            records.add(FormRecord.fromInstance(instance));
        }

        return new FormRecordSet(records);
    }
}
