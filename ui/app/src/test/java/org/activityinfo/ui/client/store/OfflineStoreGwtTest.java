package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;

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

        IndexedDB.open(new AsyncCallback<IndexedDB>() {


            @Override
            public void onSuccess(IndexedDB db) {
                db.putSchema(surveyForm, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        fail();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        // Ensure that we can also retrieve it
                        db.loadSchema(surveyForm.getId(), new IDBCallback<FormClass>() {
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

                            @Override
                            public void onFailure(JavaScriptObject error) {
                                fail();
                            }
                        });
                    }
                });

            }

            @Override
            public void onFailure(Throwable caught) {
                fail();
            }

        });

        delayTestFinish(5000);
    }
}
