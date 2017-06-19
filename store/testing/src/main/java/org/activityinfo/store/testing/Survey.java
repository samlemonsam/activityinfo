package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoPointType;
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

    public static final ResourceId PREGNANT_FIELD_ID = ResourceId.valueOf("F7");

    public static final ResourceId PRENATALE_CARE_FIELD_ID = ResourceId.valueOf("F8");

    public static final ResourceId CALCULATED_FIELD_ID = ResourceId.valueOf("F9");

    public static final ResourceId BAD_CALCULATED_FIELD_ID = ResourceId.valueOf("F10");

    public static final ResourceId GEO_POINT_FIELD_ID = ResourceId.valueOf("F11");

    public static final ResourceId WEALTH_FIELD_ID = ResourceId.valueOf("F12");


    public static final ResourceId MALE_ID = ResourceId.valueOf("G1");

    public static final ResourceId FEMALE_ID = ResourceId.valueOf("G2");

    public static final ResourceId MARRIED_ID = ResourceId.valueOf("M1");

    public static final ResourceId SINGLE_ID = ResourceId.valueOf("M2");

    public static final ResourceId PREGNANT_ID = ResourceId.valueOf("PY");

    public static final ResourceId NOT_PREGANT_ID = ResourceId.valueOf("PN");


    public static final ResourceId PRENATALE_ID = ResourceId.valueOf("PNY");

    public static final ResourceId NO_PRENATALE_ID = ResourceId.valueOf("PNN");

    public static final ResourceId WEALTH_TV = ResourceId.valueOf("W1");
    public static final ResourceId WEALTH_RADIO = ResourceId.valueOf("W2");
    public static final ResourceId WEALTH_FRIDGE = ResourceId.valueOf("W3");


    public static final int ROW_COUNT = 536;

    private final FormClass formClass;

    private final RecordGenerator recordGenerator;

    private List<FormInstance> records = null;
    private final FormField nameField;
    private final FormField ageField;
    private final FormField numChildrenField;
    private final FormField genderField;
    private final FormField marriedField;
    private final FormField dobField;
    private final FormField pregnantField;
    private final FormField prenataleCareField;
    private final FormField calculatedField;
    private final FormField geoPointField;
    private final FormField wealthField;


    public Survey() {
        formClass = new FormClass(FORM_ID);
        formClass.setLabel("Household Survey");

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

        geoPointField = formClass.addField(GEO_POINT_FIELD_ID)
                .setCode("POINT")
                .setLabel("Location of Interview")
                .setType(GeoPointType.INSTANCE)
                .setRequired(false);


        calculatedField = formClass.addField(CALCULATED_FIELD_ID)
                .setLabel("Family Size")
                .setType(new CalculatedFieldType("CHILDREN + 1"));


        formClass.addField(BAD_CALCULATED_FIELD_ID)
                .setLabel("Bad calculation")
                .setType(new CalculatedFieldType("NO_SUCH_VARIABLE + 1"));


        numChildrenField = formClass.addField(CHILDREN_FIELD_ID)
                .setCode("CHILDREN")
                .setLabel("Number of children")
                .setType(new QuantityType("children"))
                .setRequired(false);

        genderField = formClass.addField(GENDER_FIELD_ID)
                .setLabel("Gender")
                .setCode("GENDER")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(FEMALE_ID, "Female"),
                        new EnumItem(MALE_ID, "Male")));

        pregnantField = formClass.addField(PREGNANT_FIELD_ID)
                .setLabel("Are you currently pregnant?")
                .setCode("PREGNANT")
                .setRelevanceConditionExpression("GENDER==G2")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(PREGNANT_ID, "Yes"),
                        new EnumItem(NOT_PREGANT_ID, "No")));

        prenataleCareField = formClass.addField(PRENATALE_CARE_FIELD_ID)
                .setLabel("Have you received pre-natale care?")
                .setRelevanceConditionExpression("PREGNANT==PY")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(PRENATALE_ID, "Yes"),
                        new EnumItem(NO_PRENATALE_ID, "No")));

        marriedField = formClass.addField(MARRIED_FIELD_ID)
                .setCode("MARRIED")
                .setLabel("Marital Status")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(MARRIED_ID, "Married"),
                        new EnumItem(SINGLE_ID, "Single")));

        wealthField = formClass.addField(WEALTH_FIELD_ID)
                .setCode("WEALTH")
                .setLabel("Which of the follow items do you have in your household?")
                .setType(new EnumType(Cardinality.MULTIPLE,
                        new EnumItem(WEALTH_TV, "TV"),
                        new EnumItem(WEALTH_RADIO, "Radio"),
                        new EnumItem(WEALTH_FRIDGE, "Fridge")));

        recordGenerator = new RecordGenerator(formClass)
                .distribution(AGE_FIELD_ID, new IntegerGenerator(15, 99, 0.05, "years"))
                .distribution(CHILDREN_FIELD_ID, new IntegerGenerator(0, 8, 0.20, "children"));


    }

    public static RecordRef getRecordRef(int index) {
        return new RecordRef(FORM_ID, ResourceId.valueOf("c" + index));
    }

    @Override
    public ResourceId getFormId() {
        return FORM_ID;
    }

    @Override
    public FormClass getFormClass() {
        return formClass;
    }

    public RecordGenerator getGenerator() {
        return recordGenerator;
    }

    @Override
    public List<FormInstance> getRecords() {
        if(records == null) {
            this.records = recordGenerator.generate(ROW_COUNT);
        }
        return records;
    }
}
