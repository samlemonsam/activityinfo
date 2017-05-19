package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.List;

public class IntakeForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("INTAKE_FORM");

    public static final ResourceId PROTECTION_CODE_FIELD_ID = ResourceId.valueOf("F1");
    public static final ResourceId OPEN_DATE_FIELD_ID = ResourceId.valueOf("F2");

    public static final ResourceId NATIONALITY_FIELD_ID = ResourceId.valueOf("F3");
    public static final ResourceId PROBLEM_FIELD_ID = ResourceId.valueOf("F4");
    public static final ResourceId DOB_FIELD_ID = ResourceId.valueOf("F5");


    public static final ResourceId JORDANIAN_ID = ResourceId.valueOf("N1");
    public static final ResourceId PALESTINIAN_ID = ResourceId.valueOf("N2");
    public static final ResourceId SYRIAN_ID = ResourceId.valueOf("N3");

    public static final ResourceId DOCUMENTS_ID = ResourceId.valueOf("P1");
    public static final ResourceId SERVICES_ID = ResourceId.valueOf("P2");

    public static final int ROW_COUNT = 1127;

    private final FormClass formClass;
    private final FormField codeField;
    private final FormField openDateField;
    private final FormField nationalityField;
    private final FormField problemField;
    private final FormField dobField;

    private List<FormInstance> records;

    public IntakeForm() {
        formClass = new FormClass(FORM_ID)
                .setLabel("Intake Form");

        codeField = formClass.addField(PROTECTION_CODE_FIELD_ID)
                .setCode("CODE")
                .setLabel("Protection Code")
                .setRequired(true)
                .setType(new SerialNumberType());

        openDateField = formClass.addField(OPEN_DATE_FIELD_ID)
                .setCode("OPENED")
                .setLabel("Date Case Opened")
                .setRequired(true)
                .setType(LocalDateType.INSTANCE);

        nationalityField = formClass.addField(NATIONALITY_FIELD_ID)
                .setCode("NAT")
                .setLabel("Nationality")
                .setRequired(false)
                .setType(new EnumType(Cardinality.MULTIPLE,
                        new EnumItem(PALESTINIAN_ID, "Palestinian"),
                        new EnumItem(JORDANIAN_ID, "Jordanian"),
                        new EnumItem(SYRIAN_ID, "Syrian")));

        problemField = formClass.addField(PROBLEM_FIELD_ID)
                .setCode("PROBLEM")
                .setLabel("Protection Problem Encountered")
                .setRequired(false)
                .setType(new EnumType(Cardinality.MULTIPLE,
                        new EnumItem(DOCUMENTS_ID, "Documents"),
                        new EnumItem(SERVICES_ID, "Access to Services")));


        dobField = formClass.addField(DOB_FIELD_ID)
                .setCode("DOB")
                .setLabel("Date of Birth")
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
                    .distribution(DOB_FIELD_ID, new DateGenerator(dobField, 1950, 1991))
                    .distribution(NATIONALITY_FIELD_ID, new MultiEnumGenerator(nationalityField, 0.85, 0.30, 0.15))
                    .distribution(PROBLEM_FIELD_ID, new MultiEnumGenerator(problemField, 0.65, 0.75))
                    .generate(ROW_COUNT);
        }
        return records;
    }
}
