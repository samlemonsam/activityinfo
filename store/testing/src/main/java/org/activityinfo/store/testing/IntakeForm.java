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

    private static final ResourceId FORM_ID = ResourceId.valueOf("INTAKE_FORM");

    private static final ResourceId PROTECTION_CODE_FIELD_ID = ResourceId.valueOf("F1");
    private static final ResourceId OPEN_DATE_FIELD_ID = ResourceId.valueOf("F2");

    private static final ResourceId NATIONALITY_FIELD_ID = ResourceId.valueOf("F3");
    private static final ResourceId PROBLEM_FIELD_ID = ResourceId.valueOf("F4");
    private static final ResourceId DOB_FIELD_ID = ResourceId.valueOf("F5");


    private static final ResourceId JORDANIAN_ID = ResourceId.valueOf("N1");
    private static final ResourceId PALESTINIAN_ID = ResourceId.valueOf("N2");
    private static final ResourceId SYRIAN_ID = ResourceId.valueOf("N3");

    private static final ResourceId DOCUMENTS_ID = ResourceId.valueOf("P1");
    private static final ResourceId SERVICES_ID = ResourceId.valueOf("P2");

    private static final int ROW_COUNT = 1127;

    private final FormClass formClass;
    private final FormField codeField;
    private final FormField openDateField;
    private final FormField nationalityField;
    private final FormField problemField;
    private final FormField dobField;
    private final EnumItem palestinian;
    private final EnumItem jordanian;
    private final EnumItem syrian;
    private final EnumItem documents;
    private final EnumItem acccess;


    private List<FormInstance> records;
    private RecordGenerator generator;

    public IntakeForm() {
        this(new UnitTestingIds());
    }

    public IntakeForm(Ids ids) {
        formClass = new FormClass(ids.formId("INTAKE_FORM"))
                .setLabel("Intake Form");

        codeField = formClass.addField(ids.fieldId("F1"))
                .setCode("CODE")
                .setLabel("Protection Code")
                .setRequired(true)
                .setType(new SerialNumberType());

        openDateField = formClass.addField(ids.fieldId("F2"))
                .setCode("OPENED")
                .setLabel("Date Case Opened")
                .setRequired(true)
                .setType(LocalDateType.INSTANCE);

        palestinian = ids.enumItem("Palestinian");
        jordanian = ids.enumItem("Jordanian");
        syrian = ids.enumItem("Syrian");
        nationalityField = formClass.addField(ids.fieldId("F3"))
                .setCode("NAT")
                .setLabel("Nationality")
                .setRequired(false)
                .setType(new EnumType(Cardinality.MULTIPLE,
                    palestinian,
                    jordanian,
                    syrian));

        documents = ids.enumItem("Documents");
        acccess = ids.enumItem("Access to Services");

        problemField = formClass.addField(ids.fieldId("F4"))
                .setCode("PROBLEM")
                .setLabel("Protection Problem Encountered")
                .setRequired(false)
                .setType(new EnumType(Cardinality.MULTIPLE,
                    documents,
                    acccess));


        dobField = formClass.addField(ids.fieldId("F5"))
                .setCode("DOB")
                .setLabel("Date of Birth")
                .setType(LocalDateType.INSTANCE);

        generator = new RecordGenerator(ids, formClass)
                .distribution(OPEN_DATE_FIELD_ID, new DateGenerator(openDateField, 2016, 2017))
                .distribution(DOB_FIELD_ID, new DateGenerator(dobField, 1950, 1991))
                .distribution(NATIONALITY_FIELD_ID, new MultiEnumGenerator(nationalityField, 0.85, 0.30, 0.15))
                .distribution(PROBLEM_FIELD_ID, new MultiEnumGenerator(problemField, 0.65, 0.75));
    }

    public ResourceId getProtectionCodeFieldId() {
        return codeField.getId();
    }

    public ResourceId getOpenDateFieldId() {
        return openDateField.getId();
    }

    public ResourceId getNationalityFieldId() {
        return nationalityField.getId();
    }

    public ResourceId getProblemFieldId() {
        return problemField.getId();
    }

    public ResourceId getDobFieldId() {
        return dobField.getId();
    }

    public ResourceId getJordanianId() {
        return jordanian.getId();
    }

    public ResourceId getPalestinianId() {
        return palestinian.getId();
    }

    public ResourceId getSyrianId() {
        return syrian.getId();
    }

    public ResourceId getDocumentsId() {
        return documents.getId();
    }

    public ResourceId getServicesId() {
        return acccess.getId();
    }

    public int getRowCount() {
        return ROW_COUNT;
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
