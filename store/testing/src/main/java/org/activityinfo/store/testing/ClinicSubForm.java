package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.number.QuantityType;

import java.util.List;

/**
 * Monthly sub-form for clinics
 */
public class ClinicSubForm implements TestForm {

    private final FormField consultsField;
    private final FormClass formClass;
    private final LazyRecordList records;
    private final RecordGenerator generator;

    public ClinicSubForm(Ids ids, TestForm parentForm, int recordCount) {
        formClass = new FormClass(ids.formId("CLINIC_MONTHLY"));
        formClass.setParentFormId(parentForm.getFormId());
        formClass.setSubFormKind(SubFormKind.MONTHLY);
        formClass.setDatabaseId(ids.databaseId());
        formClass.setLabel("Monthly statistics");

        consultsField = new FormField(ids.fieldId("F1"));
        consultsField.setLabel("Number of consultations");
        consultsField.setCode("NUM_CONSULT");
        consultsField.setRequired(true);
        consultsField.setType(new QuantityType("patients"));
        formClass.addElement(consultsField);

        generator = new RecordGenerator(formClass);
        generator.parentForm(parentForm);
        generator.distribution(consultsField.getId(), new IntegerGenerator(50, 250, 0.0));

        records = new LazyRecordList(generator, recordCount);
    }

    @Override
    public ResourceId getFormId() {
        return formClass.getId();
    }

    public FormField getConsultsField() {
        return consultsField;
    }

    @Override
    public FormClass getFormClass() {
        return formClass;
    }

    @Override
    public List<FormInstance> getRecords() {
        return records.get();
    }

    @Override
    public Supplier<FormInstance> getGenerator() {
        return generator;
    }
}
