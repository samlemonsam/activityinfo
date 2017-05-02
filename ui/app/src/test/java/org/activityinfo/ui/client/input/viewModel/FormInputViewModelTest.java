package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.testing.IncidentForm;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FormInputViewModelTest {

    @Test
    public void testSurveyRelevance() {

        TestingFormStore store = new TestingFormStore();

        FormInputViewModelBuilder builder = new FormInputViewModelBuilder(store.getFormTree(Survey.FORM_ID).get());

        // Start with no input
        FormInputModel inputModel = new FormInputModel();

        // Is this valid?
        FormInputViewModel viewModel = builder.build(inputModel);

        assertThat("pregnant is not yet relevant", viewModel.isRelevant(Survey.PREGNANT_FIELD_ID), equalTo(false));
        assertThat("prenatale care is not relevant", viewModel.isRelevant(Survey.PRENATALE_CARE_FIELD_ID), equalTo(false));

        assertThat("form is valid", viewModel.isValid(), equalTo(false));

        // Answer the gender
        inputModel = inputModel.update(Survey.GENDER_FIELD_ID, new FieldInput(new EnumValue(Survey.FEMALE_ID)));
        viewModel = builder.build(inputModel);

        assertThat("pregnant is now relevant", viewModel.isRelevant(Survey.PREGNANT_FIELD_ID), equalTo(true));
        assertThat("prenatale care is still not relevant", viewModel.isRelevant(Survey.PRENATALE_CARE_FIELD_ID), equalTo(false));

        // Answer pregnant = yes
        inputModel = inputModel.update(Survey.PREGNANT_FIELD_ID, new FieldInput(new EnumValue(Survey.PREGNANT_ID)));
        viewModel = builder.build(inputModel);

        assertThat("pregnant is still relevant", viewModel.isRelevant(Survey.PREGNANT_FIELD_ID), equalTo(true));
        assertThat("prenatale is now relevant", viewModel.isRelevant(Survey.PRENATALE_CARE_FIELD_ID), equalTo(true));

        // Change gender = Male
        inputModel = inputModel.update(Survey.GENDER_FIELD_ID, new FieldInput(new EnumValue(Survey.MALE_ID)));
        viewModel = builder.build(inputModel);

        assertThat("pregnant is not relevant", viewModel.isRelevant(Survey.PREGNANT_FIELD_ID), equalTo(false));
        assertThat("prenatale is not relevant", viewModel.isRelevant(Survey.PRENATALE_CARE_FIELD_ID), equalTo(false));

    }

    @Test
    public void testSubFormInput() {

        TestingFormStore store = new TestingFormStore();

        FormInputViewModelBuilder builder = new FormInputViewModelBuilder(store.getFormTree(IncidentForm.FORM_ID).get());

        // Start with empty input
        FormInputModel inputModel = new FormInputModel();

        // Should see one (empty) subform record
        FormInputViewModel viewModel = builder.build(inputModel);
        SubFormFieldViewModel referralSubForm = viewModel.getSubFormField(IncidentForm.REFERRAL_FIELD_ID);

        assertThat(referralSubForm.getSubRecords(), Matchers.hasSize(1));
    }
}