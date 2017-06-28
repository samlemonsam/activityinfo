package org.activityinfo.store.testing;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.ArrayList;
import java.util.List;

public class AdminLevelForm implements TestForm {

    private final FormClass formClass;
    private final FormField nameField;
    private final RecordGenerator recordGenerator;
    private final FormField parentField;
    private String levelName;
    private int count;

    public AdminLevelForm(Ids ids, String name, int count, Optional<AdminLevelForm> parent) {
        levelName = name;
        this.count = count;

        formClass = new FormClass(ids.formId(name.toUpperCase()));
        formClass.setLabel(name);
        formClass.setDatabaseId(ids.databaseId());

        for (FormField field : ids.builtinFields()) {
            formClass.addElement(field);
        }

        if(parent.isPresent()) {
            parentField = formClass.addField(ids.fieldId("PARENT"))
                .setLabel(parent.get().getFormClass().getLabel())
                .setType(new ReferenceType(Cardinality.SINGLE, parent.get().getFormId()))
                .setRequired(true)
                .setKey(true)
                .setVisible(true);
        } else {
            parentField = null;
        }

        nameField = formClass.addField(ids.fieldId("F1"))
            .setLabel("Name")
            .setType(TextType.SIMPLE)
            .setRequired(true)
            .setKey(true)
            .setVisible(true);


        recordGenerator = new RecordGenerator(formClass);
        recordGenerator.distribution(nameField.getId(), new UniqueNameGenerator(levelName));

        if(parent.isPresent()) {
            recordGenerator.distribution(parentField.getId(), new RefGenerator(parent.get()));
        }
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
        List<FormInstance> records = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            records.add(recordGenerator.get());
        }
        return records;
    }

    @Override
    public Supplier<FormInstance> getGenerator() {
        return recordGenerator;
    }

}
