package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;

import java.util.List;

public class ReferralSubForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("REFERRAL_SUB_FORM");

    public static final ResourceId ORGANIZATION_FIELD_ID = ResourceId.valueOf("F1");

    public static final int ROW_COUNT = 2503;

    private final IncidentForm parentForm;
    private final FormClass formClass;
    private final FormField organizationField;

    private List<FormInstance> records = null;
    private RecordGenerator generator;

    public ReferralSubForm(IncidentForm parentForm) {
        this.parentForm = parentForm;
        formClass = new FormClass(FORM_ID);
        formClass.setLabel("Referral Form");
        formClass.setSubFormKind(SubFormKind.REPEATING);
        formClass.setParentFormId(parentForm.getFormId());

        organizationField = formClass.addField(ORGANIZATION_FIELD_ID)
                .setLabel("2.2 Name of organization/department receiving the referral")
                .setType(TextType.SIMPLE)
                .setRequired(true)
                .setVisible(true);

        generator = new RecordGenerator(formClass)
                .parentForm(parentForm);
    }

    @Override
    public ResourceId getFormId() {
        return FORM_ID;
    }

    @Override
    public FormClass getFormClass() {
        return formClass;
    }


    @Override
    public List<FormInstance> getRecords() {
        if(records == null) {
            this.records = generator.generate(ROW_COUNT);
        }
        return records;
    }

    @Override
    public RecordGenerator getGenerator() {
        return generator;
    }
}
