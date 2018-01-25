package org.activityinfo.ui.client.store.offline;

import com.google.common.base.Function;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import junit.framework.TestCase;
import org.activityinfo.indexedb.IDBFactoryImpl;
import org.activityinfo.model.form.*;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.store.FormStoreImpl;
import org.activityinfo.ui.client.store.http.HttpStore;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tests the compiled Javascript offline store against a real browser.
 *
 * Because of the cost/time of setting up the database, we run only a single test with
 * lots of interaction.
 *
 */
public class OfflineStoreGwtTest extends GWTTestCase {

    private static final Logger LOGGER = Logger.getLogger(OfflineStoreGwtTest.class.getName());

    private OfflineStore offlineStore;
    private HttpStore httpStore;
    private FormStoreImpl formStore;
    private Survey survey;

    @Override
    public String getModuleName() {
        return "org.activityinfo.ui.AppTest";
    }

    public void testStoreForm() {

        survey = new Survey();

        FormMetadata surveyMetadata = FormMetadata.of(1L, survey.getFormClass(), FormPermissions.readWrite());

        // First put the schema to the store

        Snapshot snapshot = new Snapshot(
                Collections.singletonList(surveyMetadata),
                Collections.singletonList(toFormRecordSet(survey)));

        offlineStore = new OfflineStore(httpStore, IDBFactoryImpl.create());
        httpStore = new HttpStore(new OfflineClientStub());
        formStore = new FormStoreImpl(httpStore, offlineStore, Scheduler.get());

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
                        .join(OfflineStoreGwtTest.this::verifyWeCanMakeChangesOffline)
                        .join(OfflineStoreGwtTest.this::verifyWeCanLoadCurrentSnapshot)
                        .join(OfflineStoreGwtTest.this::verifyWeCanQuery)
                        .then(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        LOGGER.log(Level.SEVERE, "Operation failed", caught);
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

        return formStore.getFormMetadata(survey.getFormId()).once().then(formMetadata -> {
            FormClass result = formMetadata.getSchema();
            TestCase.assertEquals(survey.getFormId(), result.getId());
            TestCase.assertEquals(survey.getFormClass().getLabel(), result.getLabel());

            FormField expectedField = survey.getFormClass().getField(survey.getGenderFieldId());
            FormField field = result.getField(survey.getGenderFieldId());

            TestCase.assertEquals(expectedField.getId(), field.getId());
            TestCase.assertEquals(expectedField.getLabel(), field.getLabel());
            return null;
        });
    }

    private Promise<Void> verifyWeCanQueryRecords(Void input) {

        QueryModel queryModel = new QueryModel(survey.getFormId());
        queryModel.selectResourceId().as("id");
        queryModel.selectField(survey.getNameFieldId()).as("name");
        queryModel.selectField(survey.getAgeFieldId()).as("age");

        return formStore.query(queryModel).once().then(columnSet -> {

            assertEquals(survey.getRowCount(), columnSet.getNumRows());

            ColumnView name = columnSet.getColumnView("name");
            ColumnView age = columnSet.getColumnView("age");

            assertEquals(survey.getRowCount(), columnSet.getNumRows());
            assertEquals("Melanie", name.get(0));
            assertEquals("Joe", name.get(1));
            assertEquals("Matilda", name.get(2));

            return null;
        });

    }

    private Promise<Void> verifyWeCanMakeChangesOffline(Void input) {

        RecordTransaction transaction = RecordTransaction.builder()
            .create(survey.getGenerator().get())
            .create(survey.getGenerator().get())
            .build();

        return formStore.updateRecords(transaction);
    }

    private Promise<Void> verifyWeCanLoadCurrentSnapshot(Void input) {
        return offlineStore.getDatabase().begin(KeyValueStore.DEF)
            .query(tx -> tx.objectStore(KeyValueStore.DEF).getCurrentSnapshot())
            .then(new Function<SnapshotStatus, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable SnapshotStatus status) {

                    assertNotNull(status);

                    return null;
                }
            });

    }


    private Promise<Void> verifyWeCanQuery(Void input) {

        QueryModel queryModel = new QueryModel(survey.getFormId());
        queryModel.selectResourceId().as("id");
        queryModel.selectExpr(survey.getGenderFieldId().asString()).as("gender");
        queryModel.selectExpr(survey.getAgeFieldId().asString()).as("age");
        queryModel.selectExpr(survey.getNameFieldId().asString()).as("name");

        return offlineStore.query(queryModel).once().then(new Function<ColumnSet, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ColumnSet columnSet) {

                assertEquals(survey.getRowCount() + 2, columnSet.getNumRows());

                return null;
            }
        });
    }

    private FormSyncSet toFormRecordSet(Survey survey) {

        List<FormInstance> records = survey.getRecords();
        FormRecord array[] = new FormRecord[records.size()];

        for (int i = 0; i < records.size(); i++) {
            array[i] = FormRecord.fromInstance(records.get(i));
        }

        return FormSyncSet.incremental(survey.getFormId().asString(), new String[0], Arrays.asList(array));
    }
}
