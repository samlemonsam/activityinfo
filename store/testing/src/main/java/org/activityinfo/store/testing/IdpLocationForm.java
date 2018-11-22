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

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.List;

/**
 * Based on a user's form that includes three key fields:
 * a province field, a location type field, and a location name field.
 */
public class IdpLocationForm implements TestForm {

    private final FormClass formClass;
    private final RecordGenerator recordGenerator;
    private int count;
    private AdminLevelForm parentForm;
    private final FormField nameField;
    private final FormField typeField;
    private final FormField adminField;

    private LazyRecordList records;

    public IdpLocationForm(Ids ids, int count, AdminLevelForm parentForm) {
        formClass = new FormClass(ids.formId("IDP_LOCATION"));
        this.count = count;
        this.parentForm = parentForm;
        formClass.setLabel("IDP Location");
        formClass.setDatabaseId(ids.databaseId());

        for (FormField field : ids.builtinFields()) {
            formClass.addElement(field);
        }

        typeField = formClass.addField(ids.fieldId("F2"))
                .setCode("TYPE")
                .setLabel("Type of Location")
                .setType(new EnumType(Cardinality.SINGLE,
                        ids.enumItem("School"),
                        ids.enumItem("Clinic"),
                        ids.enumItem("Camp")))
                .setRequired(true)
                .setKey(true)
                .setVisible(true);

        nameField = formClass.addField(ids.fieldId("F1"))
                .setCode("NAME")
                .setLabel("Name")
                .setType(TextType.SIMPLE)
                .setRequired(true)
                .setKey(true)
                .setVisible(true);


        adminField = formClass.addField(ids.fieldId("ADMIN"))
                .setLabel(parentForm.getFormClass().getLabel())
                .setType(new ReferenceType(Cardinality.SINGLE, parentForm.getFormId()))
                .setRequired(true)
                .setKey(true)
                .setVisible(true);


        recordGenerator = new RecordGenerator(formClass);
        recordGenerator.distribution(nameField.getId(), new UniqueNameGenerator("Location"));
        recordGenerator.distribution(adminField.getId(), new RefGenerator(parentForm));
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
    public List<TypedFormRecord> getRecords() {
        return records.get();
    }

    @Override
    public Supplier<TypedFormRecord> getGenerator() {
        return recordGenerator;
    }
}
