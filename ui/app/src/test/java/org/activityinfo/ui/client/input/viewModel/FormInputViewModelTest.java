package org.activityinfo.ui.client.input.viewModel;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.testing.*;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.store.TestSetup;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

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

        // Fields with invalid relevance are considered relevant
        assertTrue("field with bad relevance is relevant", viewModel.isRelevant(survey.getAgeFieldId()));


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

        FormStructure stucture = fetchStructure(recordRef);
        FormInputViewModelBuilder builder = new FormInputViewModelBuilder(store, stucture.getFormTree());

        FormInputModel inputModel = new FormInputModel(new RecordRef(survey.getFormId(), ResourceId.generateId()));

        FormInputViewModel viewModel = builder.build(inputModel, stucture.getExistingRecord());

        assertTrue(viewModel.isValid());
    }

//    @Test
//    public void testReferenceFields() {
//
//        IntakeForm intakeForm = setup.getCatalog().getIntakeForm();
//
//        FormInputViewModelBuilder builder = builderFor(setup.getCatalog().getBioDataForm());
//        FormInputModel inputModel = new FormInputModel(new RecordRef(BioDataForm.FORM_ID, ResourceId.generateId()));
//
//        FormInputViewModel viewModel = builder.build(inputModel);
//
//        ReferenceChoices choices = viewModel.getChoices(BioDataForm.PROTECTION_CODE_FIELD_ID);
//        Connection<LookupChoices> choiceView = ObservableTesting.connect(choices.getChoices());
//
//        LookupChoices choiceSet = choiceView.assertLoaded();
//
//        assertThat(choiceSet.getCount(), equalTo(IntakeForm.ROW_COUNT));
//        assertThat(choiceSet.getLabel(0), equalTo("00001"));
//    }

    @Test
    public void testSubFormInput() {

        FormInputViewModelBuilder builder =  builderFor(setup.getCatalog().getIncidentForm());
        // Start with empty input
        FormInputModel inputModel = new FormInputModel(new RecordRef(IncidentForm.FORM_ID, ResourceId.generateId()));

        // Should see one (empty) sub form record
        FormInputViewModel viewModel = builder.build(inputModel);
        RepeatingSubFormViewModel referralSubForm = viewModel.getRepeatingSubFormField(IncidentForm.REFERRAL_FIELD_ID);

        assertThat(referralSubForm.getSubRecords(), hasSize(1));

        // We can update this sub record
        SubRecordViewModel subRecord = referralSubForm.getSubRecords().get(0);
        inputModel = inputModel.update(subRecord.getRecordRef(),
                ReferralSubForm.ORGANIZATION_FIELD_ID,
                new FieldInput(TextValue.valueOf("CRS")));
        viewModel = builder.build(inputModel);
        referralSubForm = viewModel.getRepeatingSubFormField(IncidentForm.REFERRAL_FIELD_ID);

        assertThat(referralSubForm.getSubRecords(), hasSize(1));

        // Now add a second record
        inputModel = inputModel.addSubRecord(new RecordRef(ReferralSubForm.FORM_ID, ResourceId.generateId()));
        viewModel = builder.build(inputModel);
        referralSubForm = viewModel.getRepeatingSubFormField(IncidentForm.REFERRAL_FIELD_ID);

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
                .update(survey.getAgeFieldId(), new FieldInput(new Quantity(35)));

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
        FormStructure structure = fetchStructure(ref);

        FormInputViewModelBuilder builder = builderFor(structure);
        FormInputModel model = new FormInputModel(ref);

        FormInputViewModel viewModel = builder.build(model, structure.getExistingRecord());

        assertThat(viewModel.getField(intakeForm.getProtectionCodeFieldId()), equalTo(new SerialNumber(1)));
    }


    @Test
    public void testMultipleSelectPersistence() {

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


    @Test
    public void editRecordWithSubRecords() {

        IncidentForm incidentForm = setup.getCatalog().getIncidentForm();

        RecordRef rootRecordRef = incidentForm.getRecordRef(0);
        FormStructure structure = fetchStructure(rootRecordRef);

        FormInputViewModelBuilder builder = builderFor(structure);

        FormInputModel inputModel = new FormInputModel(rootRecordRef);

        FormInputViewModel viewModel = builder.build(inputModel, structure.getExistingRecord());

        RepeatingSubFormViewModel subFormField = viewModel.getRepeatingSubFormField(IncidentForm.REFERRAL_FIELD_ID);
        assertThat(subFormField.getSubRecords(), hasSize(4));

    }

    @Test
    public void inputMask() {
        IntakeForm intakeForm = setup.getCatalog().getIntakeForm();
        FormInputViewModelBuilder builder = builderFor(intakeForm);

        FormInputModel inputModel = new FormInputModel(new RecordRef(intakeForm.getFormId(), ResourceId.generateId()));

        // Fill in required fields
        inputModel = inputModel
            .update(intakeForm.getOpenDateFieldId(), new LocalDate(2017, 1, 1));

        // Does not match input mask "000"
        inputModel = inputModel.update(intakeForm.getRegNumberFieldId(), new FieldInput(TextValue.valueOf("FOOOOO")));

        FormInputViewModel viewModel = builder.build(inputModel);

        assertThat(viewModel.isValid(), equalTo(false));
        assertThat(viewModel.getValidationErrors(intakeForm.getRegNumberFieldId()), not(empty()));
    }


    @Test
    public void requiredSubFormFields() {
        BioDataForm bioDataForm = setup.getCatalog().getBioDataForm();
        IncidentForm incidentForm = setup.getCatalog().getIncidentForm();
        ReferralSubForm referralSubForm = setup.getCatalog().getReferralSubForm();

        FormInputViewModelBuilder builder = builderFor(incidentForm);

        FormInputModel inputModel = new FormInputModel(new RecordRef(incidentForm.getFormId(), ResourceId.generateId()));

        // Fill in required fields
        inputModel = inputModel
            .update(IncidentForm.PROTECTION_CODE_FIELD_ID, new ReferenceValue(bioDataForm.getRecordRef(0)));

        // Should be valid as we have only a placeholder sub form...
        FormInputViewModel viewModel = builder.build(inputModel);


        SubRecordViewModel referralRecord = viewModel.getRepeatingSubFormField(IncidentForm.REFERRAL_FIELD_ID).getSubRecords().get(0);
        assertTrue(referralRecord.isPlaceholder());

        assertThat(viewModel.isValid(), equalTo(true));

        // Now add a new referral sub form
        // Without completing all required fields, and make sure the form is invalid
        inputModel = inputModel.update(referralRecord.getRecordRef(), referralSubForm.getContactNumber().getId(), FieldInput.EMPTY);

               viewModel = builder.build(inputModel);

        referralRecord = viewModel.getRepeatingSubFormField(IncidentForm.REFERRAL_FIELD_ID).getSubRecords().get(0);

        assertFalse("subform is invalid", referralRecord.getSubFormViewModel().isValid());
        assertFalse("parent is invalid", viewModel.isValid());
    }

    @Test
    public void monthlySubForms() {
        ClinicForm clinicForm = setup.getCatalog().getClinicForm();

        FormInputViewModelBuilder builder = builderFor(clinicForm);

        ResourceId parentRecordId = ResourceId.generateId();
        FormInputModel inputModel = new FormInputModel(new RecordRef(clinicForm.getFormId(), parentRecordId));

        FormInputViewModel viewModel = builder.build(inputModel);

        // Get the sub form view model.
        // The active ref should be set to 2017-10 by default.
        ResourceId subFormFieldId = clinicForm.getSubFormField().getId();
        KeyedSubFormViewModel subForm = viewModel.getKeyedSubFormField(subFormFieldId);
        assertThat(subForm, notNullValue());

        RecordRef octoberId = new RecordRef(clinicForm.getSubForm().getFormId(),
                ResourceId.generatedPeriodSubmissionId(parentRecordId, "2017-10"));

        RecordRef novemberId = new RecordRef(clinicForm.getSubForm().getFormId(),
                ResourceId.generatedPeriodSubmissionId(parentRecordId, "2017-11"));

        assertThat(subForm.getSubRecord().getRecordRef(), equalTo(octoberId));
        assertThat(subForm.getSubRecord().getSubFormViewModel().getRecordRef(), equalTo(octoberId));

        // Update a field in the active form
        ResourceId consultCountFieldId = clinicForm.getSubForm().getConsultsField().getId();
        inputModel = inputModel.update(octoberId, consultCountFieldId, new FieldInput(new Quantity(33)));
        viewModel = builder.build(inputModel);

        FormInputViewModel subFormViewModel = viewModel.getKeyedSubFormField(subFormFieldId).getSubRecord().getSubFormViewModel();
        assertThat(subFormViewModel.getField(consultCountFieldId), equalTo(new Quantity(33)));

        // Now change the active record to another month
        inputModel = inputModel.updateActiveSubRecord(subFormFieldId, novemberId);
        viewModel = builder.build(inputModel);

        assertThat(viewModel.getKeyedSubFormField(subFormFieldId).getActiveRecordRef(), equalTo(novemberId));



    }


    private FormInputViewModelBuilder builderFor(TestForm survey) {
        return new FormInputViewModelBuilder(setup.getFormStore(), fetchStructure(survey.getFormId()).getFormTree());
    }

    private FormInputViewModelBuilder builderFor(FormStructure structure) {
        return new FormInputViewModelBuilder(setup.getFormStore(), structure.getFormTree());
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