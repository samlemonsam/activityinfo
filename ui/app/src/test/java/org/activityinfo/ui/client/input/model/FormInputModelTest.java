package org.activityinfo.ui.client.input.model;

import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.junit.Test;

/**
 * Created by alex on 16-2-17.
 */
public class FormInputModelTest {

    @Test
    public void test() {

        TestingFormStore store = new TestingFormStore();
        FormInputModel model = new FormInputModel(store.getFormTree(Survey.FORM_ID).get());
    }

}