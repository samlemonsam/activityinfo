package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;

import java.util.List;

/**
 * A form recording support to a health clinic, with
 * a monthly subform recording monthly statistics.
 */
public class ClinicForm implements TestForm {

    private static final int RECORD_COUNT = 100;

    private final ClinicSubForm subForm;

    private final FormClass formClass;
    private final FormField nameField;
    private final FormField subFormField;

    private final RecordGenerator generator;
    private final LazyRecordList records;

    public ClinicForm(Ids ids) {
        ResourceId formId = ids.formId("CLINIC");

        formClass = new FormClass(formId);
        formClass.setLabel("Health Clinic Support");
        formClass.setDatabaseId(ids.databaseId());

        for (FormField field : ids.builtinFields()) {
            formClass.addElement(field);
        }

        subForm = new ClinicSubForm(ids, this, RECORD_COUNT * 5);


        nameField = formClass.addField(ids.fieldId("F1"))
                .setLabel("Name of Clinic")
                .setType(TextType.SIMPLE)
                .setRequired(true)
                .setVisible(true);

        subFormField = formClass.addField(ids.fieldId("F2"))
                .setLabel("Monthly Reports")
                .setType(new SubFormReferenceType(subForm.getFormId()))
                .setVisible(true);

        generator = new RecordGenerator(ids, formClass);
        generator.distribution(nameField.getId(), new UniqueNameGenerator("Centre de Santé"));

        records = new LazyRecordList(generator, 100);
    }

    @Override
    public ResourceId getFormId() {
        return formClass.getId();
    }

    @Override
    public FormClass getFormClass() {
        return formClass;
    }

    public ClinicSubForm getSubForm() {
        return subForm;
    }

    @Override
    public List<FormInstance> getRecords() {
        return records.get();
    }

    @Override
    public Supplier<FormInstance> getGenerator() {
        return generator;
    }

    public FormField getSubFormField() {
        return subFormField;
    }
}
