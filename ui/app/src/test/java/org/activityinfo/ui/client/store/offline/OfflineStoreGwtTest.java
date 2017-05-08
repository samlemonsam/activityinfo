package org.activityinfo.ui.client.store.offline;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import junit.framework.TestCase;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.promise.Promise;

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
        Promise<Void> put = IDBDatabaseImpl.begin(SchemaStore.NAME)
                .readwrite()
                .execute(tx -> tx.schemas().put(surveyForm));

        // Once that's complete, verify that it can be read
        Promise<FormClass> read = put.join(done -> IDBDatabaseImpl.loadSchema(surveyForm.getId()));

        read.then(new AsyncCallback<FormClass>() {
            @Override
            public void onFailure(Throwable caught) {
                TestCase.fail();
            }

            @Override
            public void onSuccess(FormClass result) {
                TestCase.assertEquals(surveyForm.getId(), result.getId());
                TestCase.assertEquals(surveyForm.getLabel(), result.getLabel());

                FormField expectedField = surveyForm.getField(fieldId);
                FormField field = result.getField(fieldId);

                TestCase.assertEquals(expectedField.getId(), field.getId());
                TestCase.assertEquals(expectedField.getLabel(), field.getLabel());
                finishTest();
            }
        });


        delayTestFinish(5000);
    }
}
