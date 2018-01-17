package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.List;

public class VillageForm implements TestForm {

    private final FormClass formClass;
    private final RecordGenerator recordGenerator;
    private int count;
    private AdminLevelForm parentForm;
    private final FormField nameField;
    private final FormField adminField;
    private final FormField pointField;

    private LazyRecordList records;

    public VillageForm(Ids ids, int count, AdminLevelForm parentForm) {
        formClass = new FormClass(ids.formId("VILLAGE"));
        this.count = count;
        this.parentForm = parentForm;
        formClass.setLabel("Village");
        formClass.setDatabaseId(ids.databaseId());

        for (FormField field : ids.builtinFields()) {
            formClass.addElement(field);
        }

        nameField = formClass.addField(ids.fieldId("F1"))
            .setCode("NAME")
            .setLabel("Name")
            .setType(TextType.SIMPLE)
            .setRequired(true)
            .setKey(true)
            .setVisible(true);


        adminField = formClass.addField(ids.fieldId("ADMIN"))
            .setLabel(parentForm.getFormClass().getLabel())
            .setType(new ReferenceType(Cardinality.SINGLE, parentForm.getFormId()))
            .setRequired(true)
            .setKey(true)
            .setVisible(true);

        pointField = formClass.addField(ids.fieldId("GEO"))
            .setLabel(parentForm.getFormClass().getLabel())
            .setType(GeoPointType.INSTANCE)
            .setRequired(false)
            .setVisible(true);

        recordGenerator = new RecordGenerator(formClass);
        recordGenerator.distribution(nameField.getId(), new UniqueNameGenerator("Village"));
        recordGenerator.distribution(adminField.getId(), new RefGenerator(parentForm));
        records = new LazyRecordList(recordGenerator, count);

    }

    public AdminLevelForm getParentForm() {
        return parentForm;
    }

    public FormField getNameField() {
        return nameField;
    }

    public FormField getPointField() {
        return pointField;
    }

    public FormField getAdminField() {
        return adminField;
    }

    public ResourceId getAdminFieldId() {
        return adminField.getId();
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

}
