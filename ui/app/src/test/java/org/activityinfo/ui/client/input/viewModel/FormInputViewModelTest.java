package org.activityinfo.ui.client.input.viewModel;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.ObservableTesting;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.testing.*;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.store.TestSetup;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class FormInputViewModelTest {

    private TestSetup setup = new TestSetup();

    @Before
    public void setup() {
        LocaleProxy.initialize();
    }


    @Test
    public void testSurveyRelevance() {

        Survey survey = setup.getCatalog().getSurvey();

        FormInputViewModelBuilder builder = builderFor(survey);

        // Start with no input
        FormInputModel inputModel = new FormInputModel(new RecordRef(survey.getFormId(), ResourceId.generateId()));

        // Is this valid?
        FormInputViewModel viewModel = builder.build(inputModel);

        assertThat("pregnant is not yet relevant", viewModel.isRelevant(survey.getPregnantFieldId()), equalTo(false));
        assertThat("prenatale care is not relevant", viewModel.isRelevant(survey.getPrenataleCareFieldId()), equalTo(false));

        assertThat("form is valid", viewModel.isValid(), equalTo(false));

        // Answer the gender
        inputModel = inputModel.update(survey.getGenderFieldId(), new FieldInput(new EnumValue(survey.getFemaleId())));
        viewModel = builder.build(inputModel);

        assertThat("pregnant is now relevant", viewModel.isRelevant(survey.getPregnantFieldId()), equalTo(true));
        assertThat("prenatale care is still not relevant", viewModel.isRelevant(survey.getPrenataleCareFieldId()), equalTo(false));

        // Answer pregnant = yes
        inputModel = inputModel.update(survey.getPregnantFieldId(), new FieldInput(new EnumValue(survey.getPregnantId())));
        viewModel = builder.build(inputModel);

        assertThat("pregnant is still relevant", viewModel.isRelevant(survey.getPregnantFieldId()), equalTo(true));
        assertThat("prenatale is now relevant", viewModel.isRelevant(survey.getPrenataleCareFieldId()), equalTo(true));

        // Change gender = Male
        inputModel = inputModel.update(survey.getGenderFieldId(), new FieldInput(new EnumValue(survey.getMaleId())));
        viewModel = builder.build(inputModel);

        assertThat("pregnant is not relevant", viewModel.isRelevant(survey.getPregnantFieldId()), equalTo(false));
        assertThat("prenatale is not relevant", viewModel.isRelevant(survey.getPrenataleCareFieldId()), equalTo(false));

    }
    @Test
    public void testSurveyEdit() {

        TestingFormStore store = new TestingFormStore();
        Survey survey = store.getCatalog().getSurvey();

        RecordRef recordRef = survey.getRecordRef(5);

        FormInputViewModelBuilder builder = new FormInputViewModelBuilder(
            store, fetchStructure(recordRef));

        FormInputModel inputModel = new FormInputModel(new RecordRef(survey.getFormId(), ResourceId.generateId()));

        FormInputViewModel viewModel = builder.build(inputModel);

        assertTrue(viewModel.isValid());
    }

    @Test
    public void testReferenceFields() {

        IntakeForm intakeForm = setup.getCatalog().getIntakeForm();

        FormInputViewModelBuilder builder = builderFor(setup.getCatalog().getBioDataForm());
        FormInputModel inputModel = new FormInputModel(new RecordRef(BioDataForm.FORM_ID, ResourceId.generateId()));

        FormInputViewModel viewModel = builder.build(inputModel);

        ReferenceChoices choices = viewModel.getChoices(BioDataForm.PROTECTION_CODE_FIELD_ID);
        Connection<LookupChoices> choiceView = ObservableTesting.connect(choices.getChoices());

        LookupChoices choiceSet = choiceView.assertLoaded();

        assertThat(choiceSet.getCount(), equalTo(IntakeForm.ROW_COUNT));
        assertThat(choiceSet.getLabel(0), equalTo("00001"));
    }

    @Test
    public void testSubFormInput() {

        FormInputViewModelBuilder builder =  builderFor(setup.getCatalog().getIncidentForm());
        // Start with empty input
        FormInputModel inputModel = new FormInputModel(new RecordRef(IncidentForm.FORM_ID, ResourceId.generateId()));

        // Should see one (empty) sub form record
        FormInputViewModel viewModel = builder.build(inputModel);
        SubFormInputViewModel referralSubForm = viewModel.getSubFormField(IncidentForm.REFERRAL_FIELD_ID);

        assertThat(referralSubForm.getSubRecords(), hasSize(1));

        // We can update this sub record
        SubRecordViewModel subRecord = referralSubForm.getSubRecords().get(0);
        inputModel = inputModel.update(subRecord.getRecordRef(),
                ReferralSubForm.ORGANIZATION_FIELD_ID,
                new FieldInput(TextValue.valueOf("CRS")));
        viewModel = builder.build(inputModel);
        referralSubForm = viewModel.getSubFormField(IncidentForm.REFERRAL_FIELD_ID);

        assertThat(referralSubForm.getSubRecords(), hasSize(1));

        // Now add a second record
        inputModel = inputModel.addSubRecord(new RecordRef(ReferralSubForm.FORM_ID, ResourceId.generateId()));
        viewModel = builder.build(inputModel);
        referralSubForm = viewModel.getSubFormField(IncidentForm.REFERRAL_FIELD_ID);

        assertThat(referralSubForm.getSubRecords(), hasSize(2));

        // Verify that the transaction is built is correctly
        RecordTransaction tx = viewModel.buildTransaction();
        RecordUpdate[] changes = tx.getChangeArray();
        assertThat(changes.length, equalTo(3));

        RecordUpdate parentChange = changes[0];
        RecordUpdate subFormChange = changes[1];

        assertThat(parentChange.getRecordRef(), equalTo(inputModel.getRecordRef()));
        assertThat(subFormChange.getParentRecordId(), equalTo(parentChange.getRecordId().asString()));
    }

    @Test
    public void testPersistence() {

        Survey survey = setup.getCatalog().getSurvey();

        FormInputViewModelBuilder builder = builderFor(survey);

        // Start with no input
        FormInputModel inputModel = new FormInputModel(new RecordRef(survey.getFormId(), ResourceId.generateId()))
                .update(survey.getGenderFieldId(), new FieldInput(new EnumValue(survey.getMaleId())))
                .update(survey.getNameFieldId(), new FieldInput(TextValue.valueOf("BOB")))
                .update(survey.getDobFieldId(), new FieldInput(new LocalDate(1982,1,16)))
                .update(survey.getAgeFieldId(), new FieldInput(new Quantity(35, "years")));

        // Verify that it's valid
        FormInputViewModel viewModel = builder.build(inputModel);
        assertThat(viewModel.isValid(), equalTo(true));

        // Now build the update transaction and save!
        Promise<Void> completed = setup.getFormStore().updateRecords(viewModel.buildTransaction());
        assertThat(completed.getState(), equalTo(Promise.State.FULFILLED));
    }

    @Test
    public void testSerialNumberEdit() {
        IntakeForm intakeForm = setup.getCatalog().getIntakeForm();

        RecordRef ref = intakeForm.getRecords().get(0).getRef();

        FormInputViewModelBuilder builder = builderFor(ref);
        FormInputModel model = new FormInputModel(ref);

        FormInputViewModel viewModel = builder.build(model);

        assertThat(viewModel.getField(intakeForm.getProtectionCodeFieldId()), equalTo(new SerialNumber(1)));
    }


    @Test
    public void testMultipleSelectPersistance() {

        IntakeForm intakeForm = setup.getCatalog().getIntakeForm();

        FormInputViewModelBuilder builder = builderFor(intakeForm);

        FormInputModel inputModel = new FormInputModel(new RecordRef(intakeForm.getFormId(), ResourceId.generateId()))
                .update(intakeForm.getOpenDateFieldId(), new LocalDate(2017,1,1))
                .update(intakeForm.getNationalityFieldId(), new EnumValue(intakeForm.getPalestinianId(), intakeForm.getJordanianId()));

        FormInputViewModel viewModel = builder.build(inputModel);
        assertThat(viewModel.isValid(), equalTo(true));

        Promise<Void> completed = setup.getFormStore().updateRecords(viewModel.buildTransaction());
        assertThat(completed.getState(), equalTo(Promise.State.FULFILLED));
    }



    private FormInputViewModelBuilder builderFor(TestForm survey) {
        return new FormInputViewModelBuilder(setup.getFormStore(), fetchStructure(survey.getFormId()));
    }

    private FormInputViewModelBuilder builderFor(RecordRef ref) {
        return new FormInputViewModelBuilder(setup.getFormStore(), fetchStructure(ref));
    }


    private FormStructure fetchStructure(ResourceId formId) {
        ResourceId newRecordId = ResourceId.generateSubmissionId(formId);
        RecordRef newRecordRef = new RecordRef(formId, newRecordId);

        return fetchStructure(newRecordRef);
    }

    private FormStructure fetchStructure(RecordRef ref) {
        return setup.connect(FormStructure.fetch(setup.getFormStore(), ref)).assertLoaded();
    }



}