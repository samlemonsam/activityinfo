package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.List;

public class IntakeForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("INTAKE_FORM");

    public static final ResourceId PROTECTION_CODE_FIELD_ID = ResourceId.valueOf("F1");
    public static final ResourceId OPEN_DATE_FIELD_ID = ResourceId.valueOf("F2");

    public static final int ROW_COUNT = 1127;

    private final FormClass formClass;
    private final FormField codeField;
    private final FormField openDateField;

    private List<FormInstance> records;

    public IntakeForm() {
        formClass = new FormClass(FORM_ID)
                .setLabel("Intake Form");

        codeField = formClass.addField(PROTECTION_CODE_FIELD_ID)
                .setCode("CODE")
                .setLabel("Protection Code")
                .setRequired(true)
                .setType(LocalDateType.INSTANCE);

        openDateField = formClass.addField(OPEN_DATE_FIELD_ID)
                .setCode("OPENED")
                .setLabel("Date Case Opened")
                .setRequired(true)
                .setType(LocalDateType.INSTANCE);

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
            this.records = new RecordGenerator(formClass)
                    .distribution(OPEN_DATE_FIELD_ID, new DateGenerator(openDateField, 2016, 2017))
                    .generate(ROW_COUNT);
        }
        return records;
    }
}
