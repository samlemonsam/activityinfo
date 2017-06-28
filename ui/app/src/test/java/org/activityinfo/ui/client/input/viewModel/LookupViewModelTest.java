package org.activityinfo.ui.client.input.viewModel;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.observable.Connection;
import org.activityinfo.store.testing.BioDataForm;
import org.activityinfo.store.testing.IntakeForm;
import org.activityinfo.store.testing.NfiForm;
import org.activityinfo.ui.client.store.TestSetup;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class LookupViewModelTest {

    @Before
    public void setup() {
        LocaleProxy.initialize();
    }

    @Test
    public void simpleTest() {
        TestSetup setup = new TestSetup();
        IntakeForm intakeForm = setup.getCatalog().getIntakeForm();
        BioDataForm bioDataForm = setup.getBioDataForm();

        FormTree formTree = setup.getFormTree(bioDataForm.getFormId());

        LookupKeySet lookupKeySet = new LookupKeySet(formTree, bioDataForm.getCodeField());
        assertThat(lookupKeySet.getKeys(), hasSize(1));

        LookupViewModel viewModel = new LookupViewModel(setup.getFormStore(), lookupKeySet);

        LookupField field = viewModel.getField(0);

        Connection<LookupChoices> choicesView = setup.connect(field.getChoices());

        assertThat(choicesView.assertLoaded().getCount(), equalTo(IntakeForm.ROW_COUNT));
        assertThat(choicesView.assertLoaded().getId(0), equalTo(intakeForm.getRecords().get(0).getRef()));
        assertThat(choicesView.assertLoaded().getLabel(0), equalTo("00001"));
    }

    @Test
    public void hierarchy() {
        TestSetup setup = new TestSetup();

        NfiForm nfi = setup.getCatalog().getNfiForm();

        FormTree formTree = setup.getFormTree(nfi.getFormId());
        LookupKeySet lookupKeySet = new LookupKeySet(formTree, nfi.getVillageField());

        // The hierarchy we should see here is:
        // Province
        // Territory
        // Village

        assertThat(lookupKeySet.getKeys(), hasSize(3));
        assertThat(lookupKeySet.getKey(0).getKeyLabel(), equalTo("Province"));
        assertThat(lookupKeySet.getKey(1).getKeyLabel(), equalTo("Territory"));
        assertThat(lookupKeySet.getKey(2).getKeyLabel(), equalTo("Village"));

        // Construct our view
        LookupViewModel viewModel = new LookupViewModel(setup.getFormStore(), lookupKeySet);
        LookupField province = viewModel.getField(0);
        LookupField territory = viewModel.getField(1);
        LookupField village = viewModel.getField(2);


        Connection<LookupChoices> provinceView = setup.connect(province.getChoices());

        assertThat(provinceView.assertLoaded().getCount(), equalTo(setup.getCatalog().getProvince().getRecords().size()));
        assertThat(provinceView.assertLoaded().getLabel(5), equalTo("Province 6"));

        // Initially the second level should be disabled


    }

}