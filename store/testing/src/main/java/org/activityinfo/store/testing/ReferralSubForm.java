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
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;

import java.util.List;

public class ReferralSubForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("REFERRAL_SUB_FORM");

    public static final ResourceId ORGANIZATION_FIELD_ID = ResourceId.valueOf("F1");

    public static final ResourceId PHONE_FIELD_ID = ResourceId.valueOf("F2");

    public static final int ROW_COUNT = 2503;

    private final IncidentForm parentForm;
    private final FormClass formClass;
    private final FormField organizationField;
    private final FormField contactNumber;

    private List<TypedFormRecord> records = null;
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

        contactNumber = formClass.addField(PHONE_FIELD_ID)
                .setLabel("Contact phone number")
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

    public FormField getContactNumber() {
        return contactNumber;
    }

    @Override
    public List<TypedFormRecord> getRecords() {
        if(records == null) {
            this.records = generator.get(ROW_COUNT);
        }
        return records;
    }

    @Override
    public RecordGenerator getGenerator() {
        return generator;
    }
}
