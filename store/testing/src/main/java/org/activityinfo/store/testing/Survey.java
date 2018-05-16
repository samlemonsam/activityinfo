/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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


    private static final int ROW_COUNT = 536;

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
    private final EnumItem genderFemale;
    private final EnumItem genderMale;
    private final EnumItem pregnantYes;
    private final EnumItem pregnantNo;
    private final EnumItem prenataleYes;
    private final EnumItem prenataleNo;
    private final EnumItem married;
    private final EnumItem single;
    private final EnumItem wealthTv;
    private final EnumItem wealthRadio;
    private final EnumItem wealthFridge;
    private final FormField badCalculatedField;
    private final FormField spouseField;

    public Survey() {
        this(new UnitTestingIds());
    }

    public Survey(Ids ids) {
        formClass = new FormClass(ids.formId("SURVEY_FORM"));
        formClass.setLabel("Household Survey");
        formClass.setDatabaseId(ids.databaseId());

        for (FormField field : ids.builtinFields()) {
            formClass.addElement(field);
        }

        nameField = formClass.addField(ids.fieldId("F1"))
                .setCode("NAME")
                .setLabel("Respondent Name")
                .setType(TextType.SIMPLE)
                .setRequired(true)
                .setVisible(true);

        dobField = formClass.addField(ids.fieldId("F2"))
                .setCode("DOB")
                .setLabel("Respondent's Date of Birth")
                .setType(LocalDateType.INSTANCE)
                .setRequired(true)
                .setVisible(true);

        ageField = formClass.addField(ids.fieldId("F3"))
                .setCode("AGE")
                .setLabel("Respondent's Age")
                .setType(new QuantityType("years"))
                .setRelevanceConditionExpression("FOO != BAR(3)")  // Invalid expression
                .setRequired(true);

        geoPointField = formClass.addField(ids.fieldId("F4"))
                .setCode("POINT")
                .setLabel("Location of Interview")
                .setType(GeoPointType.INSTANCE)
                .setRequired(false);


        calculatedField = formClass.addField(ids.fieldId("F5"))
                .setLabel("Family Size")
                .setType(new CalculatedFieldType("(CHILDREN + 1)"));


        badCalculatedField = formClass.addField(ids.fieldId("F6"))
            .setLabel("Bad calculation")
            .setType(new CalculatedFieldType("NO_SUCH_VARIABLE + 1"));


        numChildrenField = formClass.addField(ids.fieldId("F7"))
                .setCode("CHILDREN")
                .setLabel("Number of children")
                .setType(new QuantityType("children"))
                .setRequired(false);

        genderFemale = ids.enumItem("Female");
        genderMale = ids.enumItem("Male");
        genderField = formClass.addField(ids.fieldId("F8"))
                .setLabel("Gender")
                .setCode("GENDER")
                .setType(new EnumType(Cardinality.SINGLE,
                    genderFemale,
                    genderMale));

        pregnantYes = ids.enumItem("Yes");
        pregnantNo = ids.enumItem("No");
        pregnantField = formClass.addField(ids.fieldId("F9"))
                .setLabel("Are you currently pregnant?")
                .setCode("PREGNANT")
                .setRelevanceConditionExpression("GENDER==" + genderFemale.getId().asString())
                .setType(new EnumType(Cardinality.SINGLE,
                    pregnantYes,
                    pregnantNo));

        prenataleYes = ids.enumItem("Yes");
        prenataleNo = ids.enumItem("No");
        prenataleCareField = formClass.addField(ids.fieldId("F10"))
                .setLabel("Have you received pre-natale care?")
                .setRelevanceConditionExpression("PREGNANT==" + pregnantYes.getId().asString())
                .setType(new EnumType(Cardinality.SINGLE,
                    prenataleYes,
                    prenataleNo));

        married = ids.enumItem("Married");
        single = ids.enumItem("Single");
        marriedField = formClass.addField(ids.fieldId("F11"))
                .setCode("MARRIED")
                .setLabel("Marital Status")
                .setType(new EnumType(Cardinality.SINGLE,
                    married,
                    single));

        wealthTv = ids.enumItem("TV");
        wealthRadio = ids.enumItem("Radio");
        wealthFridge = ids.enumItem("Fridge");
        wealthField = formClass.addField(ids.fieldId("F12"))
                .setCode("WEALTH")
                .setLabel("Which of the follow items do you have in your household?")
                .setType(new EnumType(Cardinality.MULTIPLE,
                    wealthTv,
                    wealthRadio,
                    wealthFridge));

        spouseField = formClass.addField(ids.fieldId("F13"))
                .setCode("SPOUSENAME")
                .setLabel("Respondent Spouse Name")
                .setType(TextType.SIMPLE)
                .setRelevanceConditionExpression("MARRIED==" + married.getId().asString())
                .setRequired(false)
                .setVisible(true);

        recordGenerator = new RecordGenerator(ids, formClass)
                .distribution(ageField.getId(), new IntegerGenerator(15, 99, 0.05))
                .distribution(numChildrenField.getId(), new IntegerGenerator(0, 8, 0.20))
                .distribution(dobField.getId(), new DateGenerator(dobField, 1850, 2010))
                .enumSeed(genderField, "F4".hashCode())
                .enumSeed(marriedField, "F5".hashCode())
                .enumSeed(pregnantField, "F7".hashCode())
                .enumSeed(prenataleCareField, "F8".hashCode());
    }

    public RecordRef getRecordRef(int index) {
        return new RecordRef(getFormId(), ResourceId.valueOf("c" + index));
    }

    public ResourceId getNameFieldId() {
        return nameField.getId();
    }

    public ResourceId getDobFieldId() {
        return dobField.getId();
    }

    public ResourceId getAgeFieldId() {
        return ageField.getId();
    }

    public ResourceId getGenderFieldId() {
        return genderField.getId();
    }

    public ResourceId getMarriedFieldId() {
        return marriedField.getId();
    }

    public ResourceId getChildrenFieldId() {
        return numChildrenField.getId();
    }

    public ResourceId getPregnantFieldId() {
        return this.pregnantField.getId();
    }

    public ResourceId getPrenataleCareFieldId() {
        return prenataleCareField.getId();
    }

    public ResourceId getCalculatedFieldId() {
        return this.calculatedField.getId();
    }

    public ResourceId getBadCalculatedFieldId() {
        return this.badCalculatedField.getId();
    }

    public ResourceId getGeoPointFieldId() {
        return geoPointField.getId();
    }

    public ResourceId getWealthFieldId() {
        return wealthField.getId();
    }

    public ResourceId getMaleId() {
        return genderMale.getId();
    }

    public ResourceId getFemaleId() {
        return genderFemale.getId();
    }

    public ResourceId getMarriedId() {
        return married.getId();
    }

    public ResourceId getSingleId() {
        return single.getId();
    }

    public ResourceId getPregnantId() {
        return pregnantYes.getId();
    }

    public ResourceId getNotPregantId() {
        return pregnantNo.getId();
    }

    public ResourceId getPrenataleId() {
        return prenataleYes.getId();
    }

    public ResourceId getNoPrenataleId() {
        return prenataleNo.getId();
    }

    public ResourceId getWealthTv() {
        return wealthTv.getId();
    }

    public String getWealthTvLabel() {
        return wealthTv.getLabel();
    }

    public ResourceId getWealthRadio() {
        return wealthRadio.getId();
    }

    public String getWealthRadioLabel() {
        return wealthRadio.getLabel();
    }

    public ResourceId getWealthFridge() {
        return wealthFridge.getId();
    }

    public String getWealthFridgeLabel() {
        return wealthFridge.getLabel();
    }

    public static int getRowCount() {
        return ROW_COUNT;
    }

    public EnumItem getPregnantYes() {
        return pregnantYes;
    }

    public EnumItem getPregnantNo() {
        return pregnantNo;
    }

    public ResourceId getSpouseFieldId() {
        return spouseField.getId();
    }

    @Override
    public ResourceId getFormId() {
        return formClass.getId();
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
            this.records = recordGenerator.get(getRowCount());
        }
        return records;
    }
}
