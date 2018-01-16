package org.activityinfo.ui.client.input.viewModel;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.formTree.FormMetadataProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.testing.*;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.store.TestSetup;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
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
        FormInputViewModelBuilder builder = new FormInputViewModelBuilder(store, stucture.getFormTree(), new TestingActivePeriodMemory());

        FormInputModel inputModel = new FormInputModel(new RecordRef(survey.getFormId(), ResourceId.generateId()));

        FormInputViewModel viewModel = builder.build(inputModel, stucture.getExistingRecord());

        assertTrue(viewModel.isValid());
    }

    @Test
    public void testNewlyIrrelvantFieldSetToEmpty() {

        TestingFormStore store = new TestingFormStore();
        Survey survey = store.getCatalog().getSurvey();

        RecordRef recordRef = survey.getRecordRef(8);

        FormStructure structure = fetchStructure(recordRef);
        FormInputViewModelBuilder builder = new FormInputViewModelBuilder(store, structure.getFormTree(), new TestingActivePeriodMemory());

        FormInputModel inputModel = new FormInputModel(new RecordRef(survey.getFormId(), ResourceId.generateId()));

        // The record was saved as GENDER=Female, and PREGNANT=No

        FormInputViewModel viewModel = builder.build(inputModel, structure.getExistingRecord());
        assertThat(viewModel.getField(survey.getGenderFieldId()), equalTo(new EnumValue(survey.getFemaleId())));
        assertThat(viewModel.isRelevant(survey.getPregnantFieldId()), equalTo(true));
        assertThat(viewModel.getField(survey.getPregnantFieldId()), equalTo(new EnumValue(survey.getPregnantNo())));

        // When we change the Gender to Male, then PREGNANT should be set to empty
        inputModel = inputModel.update(survey.getGenderFieldId(), new EnumValue(survey.getMaleId()));
        viewModel = builder.build(inputModel, structure.getExistingRecord());

        assertThat(viewModel.isRelevant(survey.getPregnantFieldId()), equalTo(false));

        RecordTransaction tx = viewModel.buildTransaction();
        assertThat(tx.getChangeArray(), arrayWithSize(1));

        RecordUpdate update = tx.getChanges().iterator().next();
        assertTrue(update.getFields().get(survey.getPregnantFieldId().asString()).isJsonNull());
    }


    @Test
    public void testSubFormInput() {

        FormInputViewModelBuilder builder =  builderFor(setup.getCatalog().getIncidentForm());
        // Start with empty input
        FormInputModel inputModel = new FormInputModel(new RecordRef(IncidentForm.FORM_ID, ResourceId.generateId()));

        // Should see one (empty) sub form record
        FormInputViewModel viewModel = builder.build(inputModel);
        SubFormViewModel referralSubForm = viewModel.getSubForm(IncidentForm.REFERRAL_FIELD_ID);

        assertThat(referralSubForm.getSubRecords(), hasSize(1));

        // We can update this sub record
        FormInputViewModel subRecord = referralSubForm.getSubRecords().get(0);
        inputModel = inputModel.update(subRecord.getRecordRef(),
                ReferralSubForm.ORGANIZATION_FIELD_ID,
                new FieldInput(TextValue.valueOf("CRS")));
        viewModel = builder.build(inputModel);
        referralSubForm = viewModel.getSubForm(IncidentForm.REFERRAL_FIELD_ID);

        assertThat(referralSubForm.getSubRecords(), hasSize(1));

        // Now add a second record
        inputModel = inputModel.addSubRecord(new RecordRef(ReferralSubForm.FORM_ID, ResourceId.generateId()));
        viewModel = builder.build(inputModel);
        referralSubForm = viewModel.getSubForm(IncidentForm.REFERRAL_FIELD_ID);

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

        SubFormViewModel subFormField = viewModel.getSubForm(IncidentForm.REFERRAL_FIELD_ID);
        assertThat(subFormField.getSubRecords(), hasSize(4));

        // Try deleting the first one...
        RecordRef deletedRef = subFormField.getSubRecords().get(0).getRecordRef();
        inputModel = inputModel.deleteSubRecord(deletedRef);
        viewModel = builder.build(inputModel, structure.getExistingRecord());
        subFormField = viewModel.getSubForm(IncidentForm.REFERRAL_FIELD_ID);

        assertThat(subFormField.getSubRecords(), hasSize(3));

        // Should show up in transaction
        RecordTransaction tx = viewModel.buildTransaction();
        assertThat(tx.getChanges(), hasItem(hasProperty("deleted", equalTo(true))));


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


        FormInputViewModel referralRecord = viewModel.getSubForm(IncidentForm.REFERRAL_FIELD_ID).getSubRecords().get(0);
        assertTrue(referralRecord.isPlaceholder());

        assertThat(viewModel.isValid(), equalTo(true));

        // Now add a new referral sub form
        // Without completing all required fields, and make sure the form is invalid
        inputModel = inputModel.update(referralRecord.getRecordRef(), referralSubForm.getContactNumber().getId(), FieldInput.EMPTY);

               viewModel = builder.build(inputModel);

        referralRecord = viewModel.getSubForm(IncidentForm.REFERRAL_FIELD_ID).getSubRecords().get(0);

        assertFalse("subform is invalid", referralRecord.isValid());
        assertFalse("parent is invalid", viewModel.isValid());
    }

    @Test
    public void monthlySubForms() {
        ClinicForm clinicForm = setup.getCatalog().getClinicForm();
        ResourceId consultCountFieldId = clinicForm.getSubForm().getConsultsField().getId();

        FormInputViewModelBuilder builder = builderFor(clinicForm);

        ResourceId parentRecordId = ResourceId.generateId();
        FormInputModel inputModel = new FormInputModel(new RecordRef(clinicForm.getFormId(), parentRecordId));

        FormInputViewModel viewModel = builder.build(inputModel);

        // Get the sub form view model.
        // The active ref should be set to 2017-10 by default.
        ResourceId subFormFieldId = clinicForm.getSubFormField().getId();
        SubFormViewModel subForm = viewModel.getSubForm(subFormFieldId);
        assertThat(subForm, notNullValue());

        RecordRef octoberId = new RecordRef(clinicForm.getSubForm().getFormId(),
                ResourceId.generatedPeriodSubmissionId(parentRecordId, "2017-10"));

        RecordRef novemberId = new RecordRef(clinicForm.getSubForm().getFormId(),
                ResourceId.generatedPeriodSubmissionId(parentRecordId, "2017-11"));

        assertThat(subForm.getActiveRecordRef(), equalTo(octoberId));

        // Update a field in the active form
        inputModel = inputModel.updateSubForm(
            subForm.update(
                consultCountFieldId,
                new FieldInput(new Quantity(33))));

        viewModel = builder.build(inputModel);

        FormInputViewModel subFormViewModel = viewModel.getSubForm(subFormFieldId).getActiveSubViewModel();
        assertThat(subFormViewModel.getField(consultCountFieldId), equalTo(new Quantity(33)));
        assertThat(subFormViewModel.isValid(), equalTo(true));

        // Now change the active record to another month
        inputModel = inputModel.updateActiveSubRecord(subFormFieldId, novemberId);

        viewModel = builder.build(inputModel);
        subForm = viewModel.getSubForm(subFormFieldId);

        // At this point, the subform is not valid because required fields are not filled in,
        // but it is also not dirty because we have not changed anything
        assertThat(subForm.getActiveSubViewModel().isValid(), equalTo(false));
        assertThat(subForm.getActiveSubViewModel().isDirty(), equalTo(false));
        assertThat(subForm.getActiveSubViewModel().isPlaceholder(), equalTo(true));

        // Now fill in the required field

        inputModel = inputModel.updateSubForm(subForm.update(consultCountFieldId, new FieldInput(new Quantity(44))));

        viewModel = builder.build(inputModel);

        assertThat(viewModel.getSubForm(subFormFieldId).getActiveRecordRef(), equalTo(novemberId));
        assertThat(viewModel.getSubForm(subFormFieldId).isValid(), equalTo(true));
    }

    /**
     * Test the ViewModel for when the user does not have access to a referenced sub form.
     */
    @Test
    public void hiddenSubForm() {

        FormClass parentForm = new FormClass(ResourceId.valueOf("PARENT_FORM"));
        parentForm.addField(ResourceId.valueOf("F1"))
                .setLabel("What is your name?")
                .setType(TextType.SIMPLE);

        parentForm.addField(ResourceId.valueOf("F2"))
                .setLabel("What are your secrets?")
                .setType(new SubFormReferenceType(ResourceId.valueOf("SECRET_FORM")));

        FormTreeBuilder treeBuilder = new FormTreeBuilder(new FormMetadataProvider() {
            @Override
            public FormMetadata getFormMetadata(ResourceId formId) {
                if(formId.equals(parentForm.getId())) {
                    return FormMetadata.of(1, parentForm, FormPermissions.owner());
                } else {
                    return FormMetadata.forbidden(formId);
                }
            }
        });

        FormTree formTree = treeBuilder.queryTree(parentForm.getId());

        FormStore formStore = EasyMock.createMock(FormStore.class);
        EasyMock.replay(formStore);

        FormInputViewModelBuilder viewModelBuilder = new FormInputViewModelBuilder(formStore, formTree, new TestingActivePeriodMemory());

        FormInputViewModel viewModel = viewModelBuilder.build(
                new FormInputModel(new RecordRef(parentForm.getId(), ResourceId.valueOf("R1"))));



    }


    private FormInputViewModelBuilder builderFor(TestForm survey) {
        return new FormInputViewModelBuilder(setup.getFormStore(), fetchStructure(survey.getFormId()).getFormTree(), new TestingActivePeriodMemory());
    }

    private FormInputViewModelBuilder builderFor(FormStructure structure) {
        return new FormInputViewModelBuilder(setup.getFormStore(), structure.getFormTree(), new TestingActivePeriodMemory());
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