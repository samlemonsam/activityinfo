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
import org.activityinfo.model.type.ReferenceType;

import java.util.List;

public class BioDataForm implements TestForm {


    public static final ResourceId FORM_ID = ResourceId.valueOf("BIODATA_FORM");

    public static final ResourceId PROTECTION_CODE_FIELD_ID = ResourceId.valueOf("F1");

    public static final int ROW_COUNT = 940;

    private final FormClass formClass;
    private final FormField codeField;

    private List<FormInstance> records = null;
    private IntakeForm intakeForm;
    private RecordGenerator generator;


    public BioDataForm(IntakeForm intakeForm) {
        this.intakeForm = intakeForm;
        formClass = new FormClass(FORM_ID);
        formClass.setLabel("Confidential BioData");

        codeField = formClass.addField(PROTECTION_CODE_FIELD_ID)
                .setCode("PCODE")
                .setLabel("Protection Code")
                .setType(new ReferenceType(Cardinality.SINGLE, intakeForm.getFormId()))
                .setRequired(true)
                .setKey(true)
                .setVisible(true);

        generator = new RecordGenerator(formClass)
                .distribution(PROTECTION_CODE_FIELD_ID, new RefKeyGenerator(intakeForm));
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

    public RecordRef getRecordRef(int i) {
        return getRecords().get(i).getRef();
    }

    public FormField getCodeField() {
        return codeField;
    }
}
