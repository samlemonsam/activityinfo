package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;

/**
 * An example of a Survey Form
 */
public class Survey implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("SURVEY_FORM");

    public static final ResourceId NAME_FIELD_ID = ResourceId.valueOf("F1");

    public static final ResourceId DOB_FIELD_ID = ResourceId.valueOf("F2");

    public static final ResourceId AGE_FIELD_ID = ResourceId.valueOf("F3");

    public static final ResourceId GENDER_FIELD_ID = ResourceId.valueOf("F4");

    public static final ResourceId MALE_ID = ResourceId.valueOf("G1");

    public static final ResourceId FEMALE_ID = ResourceId.valueOf("G2");
    private final FormClass formClass;

    public Survey() {
        formClass = new FormClass(FORM_ID);
        formClass.addField(NAME_FIELD_ID)
                .setCode("NAME")
                .setLabel("Respondent Name")
                .setType(TextType.INSTANCE)
                .setRequired(true)
                .setVisible(true);

        formClass.addField(DOB_FIELD_ID)
                .setCode("DOB")
                .setLabel("Respondent's Date of Birth")
                .setType(LocalDateType.INSTANCE)
                .setRequired(true)
                .setVisible(true);

        formClass.addField(AGE_FIELD_ID)
                .setCode("AGE")
                .setLabel("Respondent's Age")
                .setType(new QuantityType("years"))
                .setRequired(true);


        formClass.addField(GENDER_FIELD_ID)
                .setLabel("Gender")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(FEMALE_ID, "Female"),
                        new EnumItem(MALE_ID, "Male")));

    }

    @Override
    public ResourceId getFormId() {
        return FORM_ID;
    }

    @Override
    public FormClass getFormClass() {
        return formClass;
    }
}
