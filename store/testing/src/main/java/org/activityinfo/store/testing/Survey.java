package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.List;

/**
 * An example of a Survey Form
 */
public class Survey implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("SURVEY_FORM");

    public static final ResourceId NAME_FIELD_ID = ResourceId.valueOf("F1");

    public static final ResourceId DOB_FIELD_ID = ResourceId.valueOf("F2");

    public static final ResourceId AGE_FIELD_ID = ResourceId.valueOf("F3");

    public static final ResourceId GENDER_FIELD_ID = ResourceId.valueOf("F4");

    public static final ResourceId MARRIED_FIELD_ID = ResourceId.valueOf("F5");

    public static final ResourceId CHILDREN_FIELD_ID = ResourceId.valueOf("F6");

    public static final ResourceId MALE_ID = ResourceId.valueOf("G1");

    public static final ResourceId FEMALE_ID = ResourceId.valueOf("G2");


    public static final ResourceId MARRIED_ID = ResourceId.valueOf("M1");

    public static final ResourceId SINGLE_ID = ResourceId.valueOf("M2");

    public static final int ROW_COUNT = 536;

    private final FormClass formClass;
    private final FormField dobField;

    private List<FormInstance> records = null;
    private final FormField nameField;
    private final FormField ageField;
    private final FormField numChildrenField;
    private final FormField genderField;
    private final FormField marriedField;

    public Survey() {
        formClass = new FormClass(FORM_ID);
        nameField = formClass.addField(NAME_FIELD_ID)
                .setCode("NAME")
                .setLabel("Respondent Name")
                .setType(TextType.SIMPLE)
                .setRequired(true)
                .setVisible(true);

        dobField = formClass.addField(DOB_FIELD_ID)
                .setCode("DOB")
                .setLabel("Respondent's Date of Birth")
                .setType(LocalDateType.INSTANCE)
                .setRequired(true)
                .setVisible(true);

        ageField = formClass.addField(AGE_FIELD_ID)
                .setCode("AGE")
                .setLabel("Respondent's Age")
                .setType(new QuantityType("years"))
                .setRequired(true);


        numChildrenField = formClass.addField(CHILDREN_FIELD_ID)
                .setCode("CHILDREN")
                .setLabel("Number of children")
                .setType(new QuantityType("children"))
                .setRequired(false);

        genderField = formClass.addField(GENDER_FIELD_ID)
                .setLabel("Gender")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(FEMALE_ID, "Female"),
                        new EnumItem(MALE_ID, "Male")));

        marriedField = formClass.addField(MARRIED_FIELD_ID)
                .setCode("MARRIED")
                .setLabel("Marital Status")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(MARRIED_ID, "Married"),
                        new EnumItem(SINGLE_ID, "Single")));

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
                    .distribution(AGE_FIELD_ID, new IntegerGenerator(15, 99, 0.05, "years"))
                    .distribution(CHILDREN_FIELD_ID, new IntegerGenerator(0, 8, 0.20, "children"))
                    .generate(ROW_COUNT);
        }
        return records;
    }
}
