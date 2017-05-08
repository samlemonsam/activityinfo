package org.activityinfo.ui.client.store;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.offline.SchemaStore;

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


        // First put the schema to the store
        Promise<Void> put = IndexedDB.begin(SchemaStore.NAME)
                .readwrite()
                .execute(tx -> tx.schemas().put(surveyForm));

        // Once that's complete, verify that it can be read
        Promise<FormClass> read = put.join(done -> IndexedDB.loadSchema(surveyForm.getId()));

        read.then(new AsyncCallback<FormClass>() {
            @Override
            public void onFailure(Throwable caught) {
                fail();
            }

            @Override
            public void onSuccess(FormClass result) {
                assertEquals(surveyForm.getId(), result.getId());
                assertEquals(surveyForm.getLabel(), result.getLabel());

                FormField expectedField = surveyForm.getField(fieldId);
                FormField field = result.getField(fieldId);

                assertEquals(expectedField.getId(), field.getId());
                assertEquals(expectedField.getLabel(), field.getLabel());
                finishTest();
            }
        });


        delayTestFinish(5000);
    }
}
