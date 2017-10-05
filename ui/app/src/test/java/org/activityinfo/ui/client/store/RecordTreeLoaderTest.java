package org.activityinfo.ui.client.store;

import com.google.common.collect.Iterables;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;
import org.activityinfo.store.testing.BioDataForm;
import org.activityinfo.store.testing.IncidentForm;
import org.activityinfo.store.testing.ReferralSubForm;
import org.activityinfo.store.testing.Survey;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class RecordTreeLoaderTest {

    private TestSetup setup = new TestSetup();

    @Before
    public void setup() {
        LocaleProxy.initialize();
    }

    @Test
    public void simpleSurvey() {

        Survey survey = setup.getSurveyForm();

        Observable<Maybe<RecordTree>> recordTree = setup.getFormStore().getRecordTree(survey.getRecordRef(0));

        Connection<Maybe<RecordTree>> recordTreeView = setup.connect(recordTree);

        RecordTree tree = recordTreeView.assertLoaded().get();

        assertThat(tree.getRoot().get(survey.getNameFieldId()), equalTo(TextValue.valueOf("Melanie")));
    }

    @Test
    public void references() {
        BioDataForm bioDataForm = setup.getBioDataForm();


        Observable<Maybe<RecordTree>> recordTree = setup.getFormStore().getRecordTree(bioDataForm.getRecordRef(0));

        Connection<Maybe<RecordTree>> recordTreeView = setup.connect(recordTree);

        RecordTree tree = recordTreeView.assertLoaded().get();

        LookupKeySet lookupKeySet = new LookupKeySet(
            tree.getFormTree(),
            tree.getFormTree().getRootField(BioDataForm.PROTECTION_CODE_FIELD_ID).getField());

        assertThat(lookupKeySet.getLookupKeys(), hasSize(1));

        ReferenceValue referenceValue = (ReferenceValue) tree.getRoot().get(BioDataForm.PROTECTION_CODE_FIELD_ID);
        RecordRef ref = referenceValue.getOnlyReference();

        assertThat(lookupKeySet.label(tree, ref), equalTo("00667"));

    }

    @Test
    public void subforms() {

        IncidentForm incidentForm = setup.getCatalog().getIncidentForm();

        RecordRef rootRecordRef = incidentForm.getRecordRef(0);

        Observable<Maybe<RecordTree>> recordTree = setup.getFormStore().getRecordTree(rootRecordRef);
        Connection<Maybe<RecordTree>> recordTreeView = setup.connect(recordTree);

        Iterable<FormInstance> subRecords = recordTreeView.assertLoaded().get().getSubRecords(rootRecordRef, ReferralSubForm.FORM_ID);

        assertThat(Iterables.size(subRecords), equalTo(4));

    }

}