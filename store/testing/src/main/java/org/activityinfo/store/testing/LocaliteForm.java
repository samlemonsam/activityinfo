package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A "classic" location type that can be linked EITHER to
 * a province, a territory, or sector.
 */
public class LocaliteForm implements TestForm {

    private final FormClass formClass;
    private final FormField nameField;
    private final FormField adminField;
    private final List<AdminLevelForm> adminLevels;
    private final RecordGenerator recordGenerator;
    private final LazyRecordList records;

    public LocaliteForm(Ids ids, int count, AdminLevelForm... adminLevels) {
        formClass = new FormClass(ids.formId("VILLAGE"));
        this.adminLevels = Arrays.asList(adminLevels);
        formClass.setLabel("Village");
        formClass.setDatabaseId(ids.databaseId());

        for (FormField field : ids.builtinFields()) {
            formClass.addElement(field);
        }

        List<ResourceId> adminLevelIds = new ArrayList<>();
        for (AdminLevelForm adminLevel : adminLevels) {
            adminLevelIds.add(adminLevel.getFormId());
        }

        nameField = formClass.addField(ids.fieldId("F1"))
                .setCode("NAME")
                .setLabel("Name")
                .setType(TextType.SIMPLE)
                .setRequired(true)
                .setKey(true)
                .setVisible(true);


        adminField = formClass.addField(ids.fieldId("ADMIN"))
                .setLabel("Admin Level")
                .setType(new ReferenceType(Cardinality.SINGLE, adminLevelIds))
                .setRequired(true)
                .setKey(true)
                .setVisible(true);


        recordGenerator = new RecordGenerator(formClass);
        recordGenerator.distribution(nameField.getId(), new UniqueNameGenerator("Village"));
        recordGenerator.distribution(adminField.getId(), new RefGenerator(adminLevels));
        records = new LazyRecordList(recordGenerator, count);

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
