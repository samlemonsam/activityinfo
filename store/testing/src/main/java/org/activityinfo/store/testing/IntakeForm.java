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
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.InputMask;
import org.activityinfo.model.type.primitive.TextType;
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

    public static final int ROW_COUNT = 1127;

    private final FormClass formClass;
    private final FormField codeField;
    private final FormField openDateField;
    private final FormField nationalityField;
    private final FormField problemField;
    private final FormField dobField;
    private final FormField quantityField;
    private final FormField addressField;
    private final EnumItem palestinian;
    private final EnumItem jordanian;
    private final EnumItem syrian;
    private final EnumItem documents;
    private final EnumItem access;


    private List<FormInstance> records;
    private RecordGenerator generator;
    private FormField regNumberField;

    public IntakeForm() {
        this(new UnitTestingIds());
    }

    public IntakeForm(Ids ids) {
        formClass = new FormClass(ids.formId("INTAKE_FORM"))
                .setLabel("Intake Form");
        formClass.setDatabaseId(ids.databaseId());

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
        access = ids.enumItem("Access to Services");

        problemField = formClass.addField(ids.fieldId("F4"))
                .setCode("PROBLEM")
                .setLabel("Protection Problem Encountered")
                .setRequired(false)
                .setType(new EnumType(Cardinality.MULTIPLE,
                    documents,
                    access));

        quantityField = formClass.addField(ids.fieldId("Q1"))
                .setLabel("How many people do you take care of?")
                .setRequired(true)
                .setType(new QuantityType("people"));


        dobField = formClass.addField(ids.fieldId("F5"))
                .setCode("DOB")
                .setLabel("Date of Birth")
                .setType(LocalDateType.INSTANCE);

        regNumberField = formClass.addField(ids.fieldId("F6"))
                .setLabel("Registration Number")
                .setType(TextType.SIMPLE.withInputMask("000"));

        addressField = formClass.addField(ids.fieldId("F10"))
                .setLabel("Address")
                .setType(NarrativeType.INSTANCE);

        generator = new RecordGenerator(ids, formClass)
                .distribution(openDateField.getId(), new DateGenerator(openDateField, 2016, 2017))
                .distribution(dobField.getId(), new DateGenerator(dobField, 1950, 1991))
                .distribution(nationalityField.getId(), new MultiEnumGenerator(nationalityField, 0.85, 0.30, 0.15))
                .distribution(problemField.getId(), new MultiEnumGenerator(problemField, 0.65, 0.75))
                .distribution(regNumberField.getId(), new InputMaskGenerator(new InputMask("000"), 0.15, 0.30))
                .distribution(quantityField.getId(), new IntegerGenerator( 0, 10, 0d))
                .distribution(addressField.getId(), new AddressGenerator());
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
        return access.getId();
    }

    public QueryModel queryAll() {
        QueryModel model = new QueryModel(getFormId());
        model.selectRecordId().as("$id");
        model.selectField(codeField.getId()).as("code");
        model.selectField(openDateField.getId()).as("open_date");
        model.selectExpr("NAT.palestinian").as("palestinian");
        model.selectExpr("NAT.jordianian").as("jordanian");
        model.selectExpr("NAT.syrian").as("syrian");
        model.selectExpr("dob").as("dob");
        model.selectExpr("address").as("address");
        return model;
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
            this.records = generator.get(ROW_COUNT);
        }
        return records;
    }

    @Override
    public RecordGenerator getGenerator() {
        return generator;
    }


    public ResourceId getRegNumberFieldId() {
        return regNumberField.getId();
    }
}
