package org.activityinfo.ui.client.store.offline;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import junit.framework.TestCase;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.ui.client.store.Snapshot;

import java.util.Collections;

/**
 * Tests the compiled Javascript offline store against a real browser.
 */
public class OfflineStoreGwtTest extends GWTTestCase {
    @Override
    public String getModuleName() {
        return "org.activityinfo.ui.App";
    }

    public void testStoreForm() {

        FormClass surveyForm = new FormClass(ResourceId.valueOf("S1"));
        surveyForm.setLabel("Household Survey");

        ResourceId fieldId = ResourceId.valueOf("F1");
        surveyForm.addField(fieldId)
                .setLabel("Name of Respondant")
                .setType(TextType.SIMPLE);

        FormMetadata surveyMetadata = new FormMetadata();
        surveyMetadata.setId(surveyForm.getId());
        surveyMetadata.setVersion(1);
        surveyMetadata.setSchemaVersion(1);
        surveyMetadata.setSchema(surveyForm);


        // First put the schema to the store

        Snapshot snapshot = new Snapshot(Collections.singletonList(surveyMetadata), Collections.emptyList());

        OfflineStore store  = new OfflineStore(new IDBExecutorImpl());
        store.store(snapshot).then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                fail();
            }

            @Override
            public void onSuccess(Void result) {

                // Once the store is complete,
                // verify that the Form Schema can be read...

                Observable<FormMetadata> cachedMetadata = store.getCachedMetadata(surveyForm.getId());
                cachedMetadata.subscribe(new Observer<FormMetadata>() {
                    @Override
                    public void onChange(Observable<FormMetadata> observable) {
                        if(observable.isLoaded()) {
                            FormClass result = observable.get().getSchema();
                            TestCase.assertEquals(surveyForm.getId(), result.getId());
                            TestCase.assertEquals(surveyForm.getLabel(), result.getLabel());

                            FormField expectedField = surveyForm.getField(fieldId);
                            FormField field = result.getField(fieldId);

                            TestCase.assertEquals(expectedField.getId(), field.getId());
                            TestCase.assertEquals(expectedField.getLabel(), field.getLabel());
                            finishTest();
                        }
                    }
                });
            }
        });

        delayTestFinish(5000);
    }
}
