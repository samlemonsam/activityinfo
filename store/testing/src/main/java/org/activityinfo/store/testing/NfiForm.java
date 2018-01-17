package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.List;

/**
 * Test data set for the classic "Non Food Items" (NFI) distribution use case
 */
public class NfiForm implements TestForm {


    private final FormClass formClass;
    private final FormField dateField;
    private final FormField villageField;
    private final RecordGenerator recordGenerator;
    private final LazyRecordList records;

    public NfiForm(Ids ids, VillageForm villageForm) {
        formClass = new FormClass(ids.formId("NFI"));
        formClass.setLabel("NFI Distribution");
        formClass.setDatabaseId(ids.databaseId());

        for (FormField field : ids.builtinFields()) {
            formClass.addElement(field);
        }

        dateField = formClass.addField(ids.fieldId("F1"))
            .setLabel("Date of distribution")
            .setType(LocalDateType.INSTANCE)
            .setRequired(true)
            .setVisible(true);

        villageField = formClass.addField(ids.fieldId("F2"))
            .setLabel("Village")
            .setType(new ReferenceType(Cardinality.SINGLE, villageForm.getFormId()))
            .setRequired(true)
            .setVisible(true);

        recordGenerator = new RecordGenerator(formClass)
                .distribution(villageField.getId(), new RefGenerator(villageForm));
        records = new LazyRecordList(recordGenerator, 821);
    }

    public RecordRef getRecordRef(int index) {
        return getRecords().get(index).getRef();
    }

    @Override
    public ResourceId getFormId() {
        return formClass.getId();
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
        return recordGenerator;
    }

    public FormField getDateField() {
        return dateField;
    }

    public FormField getVillageField() {
        return villageField;
    }
}
