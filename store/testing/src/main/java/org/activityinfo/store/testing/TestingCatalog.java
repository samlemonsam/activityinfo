package org.activityinfo.store.testing;

import com.google.common.base.Optional;
import com.google.gwt.core.shared.GwtIncompatible;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.TransactionBuilder;
import org.activityinfo.model.resource.UpdateBuilder;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.spi.BlobAuthorizerStub;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.SerialNumberProvider;

import java.util.*;

@GwtIncompatible
public class TestingCatalog implements FormCatalog {

    private Map<ResourceId, TestingFormStorage> formMap = new HashMap<>();


    private SerialNumberProvider serialNumberProvider = new SerialNumberProvider() {
        @Override
        public int next(ResourceId formId, ResourceId fieldId, String prefix) {
            TestingFormStorage testingFormStorage = formMap.get(formId);
            assert testingFormStorage != null;
            return testingFormStorage.nextSerialNumber(fieldId, prefix);
        }
    };
    private final Survey survey;
    private final IntakeForm intake;
    private final BioDataForm bioData;
    private final IncidentForm incidentForm;
    private final ReferralSubForm referralSubForm;

    public TestingCatalog() {

        // Survey Use case
        survey = new Survey();
        add(survey);

        // Case Tracking use case
        intake = new IntakeForm();
        bioData = new BioDataForm(intake);
        incidentForm = new IncidentForm(bioData);
        referralSubForm = new ReferralSubForm(incidentForm);
        add(intake, bioData, incidentForm, referralSubForm);

    }

    public Survey getSurvey() {
        return survey;
    }

    public IntakeForm getIntakeForm() {
        return intake;
    }

    private void add(TestForm... testForms) {
        for (TestForm testForm : testForms) {
            assert testForm.getFormClass().getLabel() != null : testForm.getFormId() + " is missing label";
            formMap.put(testForm.getFormId(), new TestingFormStorage(testForm));
        }
    }


    @Override
    public FormClass getFormClass(ResourceId formId) {
        if (!formMap.containsKey(formId)) {
            throw new IllegalArgumentException("No such form " + formId);
        }
        return formMap.get(formId).getFormClass();
    }

    public FormTree getFormTree(ResourceId formId) {
        FormTreeBuilder builder = new FormTreeBuilder(this);
        return builder.queryTree(formId);
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        return null;
    }

    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {
        return Optional.<FormStorage>fromNullable(formMap.get(formId));
    }

    @Override
    public List<CatalogEntry> getRootEntries() {
        return Collections.emptyList();
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId, int userId) {
        return Collections.emptyList();
    }

    public ColumnSet query(QueryModel queryModel) {
        ColumnSetBuilder builder = new ColumnSetBuilder(this, new NullFormScanCache(), new NullFormSupervisor());
        return builder.build(queryModel);
    }

    public void updateRecords(TransactionBuilder transaction) {
        Updater updater = new Updater(this, 1, new BlobAuthorizerStub(), serialNumberProvider);
        updater.execute(transaction.build());
    }

    public void deleteForm(ResourceId formId) {
        formMap.remove(formId);
    }

    public UpdateBuilder addNew(ResourceId formId) {
        TestingFormStorage form = formMap.get(formId);
        if(form == null) {
            throw new RuntimeException("No such form: " + formId);
        }
        FormInstance newRecord = form.getGenerator().generate();
        UpdateBuilder update = new UpdateBuilder();
        update.setFormId(formId);
        update.setRecordId(newRecord.getRef().getRecordId());
        for (Map.Entry<ResourceId, FieldValue> entry : newRecord.getFieldValueMap().entrySet()) {
            update.setProperty(entry.getKey(), entry.getValue());
        }

        return update;
    }

}
