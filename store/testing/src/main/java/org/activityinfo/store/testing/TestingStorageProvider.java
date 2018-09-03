/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.store.testing;

import com.google.common.base.Optional;
import com.google.gwt.core.shared.GwtIncompatible;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.spi.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@GwtIncompatible
public class TestingStorageProvider implements FormStorageProvider, TransactionalStorageProvider {

    static {
        LocaleProxy.initialize();
    }

    private final LocaliteForm localiteForm;
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
    private final Survey2 survey2;
    private final IntakeForm intake;
    private final BioDataForm bioData;
    private final IncidentForm incidentForm;
    private final ReferralSubForm referralSubForm;

    private final GenericForm generic;
    private final BlankSubForm blankSubForm;

    private final AdminLevelForm province;
    private final AdminLevelForm territory;
    private final AdminLevelForm healthZone;

    private final VillageForm villageForm;
    private final NfiForm nfiForm;
    private final ClinicForm clinicForm;
    private final IdpLocationForm idpLocationForm;
    private final LocationSelectionForm locationSelectionForm;

    private final SimpleReferenceForm simpleReferenceForm;
    private final MultipleTextKeysForm multipleTextKeysForm;


    public TestingStorageProvider() {

        // Survey Use case
        survey = new Survey();
        add(survey);

        // Updated Survey Case
        survey2 = new Survey2();
        add(survey2);

        // Case Tracking use case
        intake = new IntakeForm();
        bioData = new BioDataForm(intake);
        incidentForm = new IncidentForm(bioData);
        referralSubForm = new ReferralSubForm(incidentForm);
        add(intake, bioData, incidentForm, referralSubForm);

        // Generic form setup with blank subform
        generic = new GenericForm();
        blankSubForm = new BlankSubForm(generic);
        add(generic, blankSubForm);

        // Classic NFI use case
        province = new AdminLevelForm(new UnitTestingIds(), "Province", 16, Optional.<AdminLevelForm>absent());
        territory = new AdminLevelForm(new UnitTestingIds(), "Territory", 140, Optional.of(province));
        healthZone = new AdminLevelForm(new UnitTestingIds(), "Zone de Sante", 200, Optional.of(province));

        villageForm = new VillageForm(new UnitTestingIds(), 140*10, territory);
        localiteForm = new LocaliteForm(new UnitTestingIds(), 250, province, territory, healthZone);
        nfiForm = new NfiForm(new UnitTestingIds(), villageForm);
        locationSelectionForm = new LocationSelectionForm(new UnitTestingIds(), localiteForm);
        add(province, territory, healthZone, villageForm, localiteForm, nfiForm, locationSelectionForm);

        // Empty form
        EmptyForm empty = new EmptyForm();
        add(empty);

        // Clinic form with subform
        clinicForm = new ClinicForm(new UnitTestingIds());
        add(clinicForm, clinicForm.getSubForm());

        // List of locations
        idpLocationForm = new IdpLocationForm(new UnitTestingIds(), 100, province);
        add(idpLocationForm);

        // multiple text keys form
        multipleTextKeysForm = new MultipleTextKeysForm(new UnitTestingIds());
        simpleReferenceForm = new SimpleReferenceForm(new UnitTestingIds(), multipleTextKeysForm);
        add(simpleReferenceForm, multipleTextKeysForm);
    }

    public Survey getSurvey() {
        return survey;
    }

    public Survey2 getSurvey2() {
        return survey2;
    }

    public IntakeForm getIntakeForm() {
        return intake;
    }

    private void add(TestForm... testForms) {
        for (TestForm testForm : testForms) {
            assert !formMap.containsKey(testForm.getFormId()) :
                    testForm.getClass().getName() + " has duplicate form id: " + testForm.getFormId();

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
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {
        return Optional.<FormStorage>fromNullable(formMap.get(formId));
    }

    public ColumnSet query(QueryModel queryModel) {
        ColumnSetBuilder builder = new ColumnSetBuilder(this, new NullFormScanCache(), new NullFormSupervisor());
        return builder.build(queryModel);
    }

    public void updateRecords(RecordTransaction transaction) {
        Updater updater = new Updater(this, 1, new BlobAuthorizerStub(), serialNumberProvider);
        updater.execute(transaction);
    }

    public void deleteForm(ResourceId formId) {
        formMap.remove(formId);
    }

    public RecordUpdate addNew(ResourceId formId) {
        TestingFormStorage form = formMap.get(formId);
        if(form == null) {
            throw new RuntimeException("No such form: " + formId);
        }
        FormInstance newRecord = form.getGenerator().get();
        RecordUpdate update = new RecordUpdate();
        update.setFormId(formId);
        update.setRecordId(newRecord.getRef().getRecordId());
        for (Map.Entry<ResourceId, FieldValue> entry : newRecord.getFieldValueMap().entrySet()) {
            update.setFieldValue(entry.getKey(), entry.getValue());
        }

        return update;
    }

    public BioDataForm getBioDataForm() {
        return bioData;
    }

    public AdminLevelForm getProvince() {
        return province;
    }

    public AdminLevelForm getTerritory() {
        return territory;
    }

    public VillageForm getVillageForm() {
        return villageForm;
    }

    public NfiForm getNfiForm() {
        return nfiForm;
    }

    public IncidentForm getIncidentForm() {
        return incidentForm;
    }

    public ReferralSubForm getReferralSubForm() {
        return referralSubForm;
    }

    public ClinicForm getClinicForm() {
        return clinicForm;
    }

    public IdpLocationForm getIdpLocationForm() {
        return idpLocationForm;
    }

    public GenericForm getGenericForm() {
        return generic;
    }

    public BlankSubForm getBlankSubForm() {
        return blankSubForm;
    }

    public LocaliteForm getLocaliteForm() {
        return localiteForm;
    }

    public LocationSelectionForm getLocationSelectionForm() {
        return locationSelectionForm;
    }

    public SimpleReferenceForm getSimpleReferenceForm() {
        return simpleReferenceForm;
    }

    public MultipleTextKeysForm getMultipleTextKeysForm() {
        return multipleTextKeysForm;
    }

    @Override
    public void begin() {

    }

    @Override
    public void commit() {
    }

    @Override
    public void rollback() {
    }

    public AdminLevelForm getHealthZone() {
        return healthZone;
    }
}
