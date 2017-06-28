package org.activityinfo.ui.client.store;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.testing.BioDataForm;
import org.activityinfo.store.testing.Survey;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class RecordTreeLoaderTest {

    private TestSetup setup = new TestSetup();

    @Before
    public void setup() {
        LocaleProxy.initialize();
    }

    @Test
    public void simpleSurvey() {

        Survey survey = setup.getSurveyForm();

        Observable<RecordTree> recordTree = setup.getFormStore().getRecordTree(survey.getRecordRef(0));

        Connection<RecordTree> recordTreeView = setup.connect(recordTree);

        RecordTree tree = recordTreeView.assertLoaded();

        assertThat(tree.getRoot().get(survey.getNameFieldId()), equalTo(TextValue.valueOf("Melanie")));
    }

    @Test
    public void references() {
        BioDataForm bioDataForm = setup.getBioDataForm();


        Observable<RecordTree> recordTree = setup.getFormStore().getRecordTree(bioDataForm.getRecordRef(0));

        Connection<RecordTree> recordTreeView = setup.connect(recordTree);

        RecordTree tree = recordTreeView.assertLoaded();

        LookupKeySet lookupKeySet = new LookupKeySet(
            tree.getFormTree(),
            tree.getFormTree().getRootField(BioDataForm.PROTECTION_CODE_FIELD_ID).getField());

        assertThat(lookupKeySet.getKeys(), hasSize(1));

        ReferenceValue referenceValue = (ReferenceValue) tree.getRoot().get(BioDataForm.PROTECTION_CODE_FIELD_ID);
        RecordRef ref = referenceValue.getOnlyReference();

        assertThat(lookupKeySet.label(tree, ref), equalTo("00667"));

    }

}