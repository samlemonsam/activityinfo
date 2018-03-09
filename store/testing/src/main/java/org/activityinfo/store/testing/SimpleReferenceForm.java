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
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;

import java.util.List;

public class SimpleReferenceForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("SIMPLE_REF_FORM");

    public static final ResourceId REF_FIELD_ID = ResourceId.valueOf("R1");

    public static final int ROW_COUNT = 5;

    private final FormClass formClass;
    private final FormField refField;
    private final TestForm referencedForm;

    private List<FormInstance> records;
    private RecordGenerator generator;

    public SimpleReferenceForm(Ids ids, TestForm referencedForm) {
        this.referencedForm = referencedForm;
        formClass = new FormClass(FORM_ID);
        formClass.setLabel("Simple Reference Form");
        formClass.setDatabaseId(ids.databaseId());

        refField = formClass.addField(REF_FIELD_ID)
                .setCode("REF")
                .setLabel("Reference Field")
                .setType(new ReferenceType(Cardinality.SINGLE, referencedForm.getFormId()))
                .setRequired(true)
                .setVisible(true);

        generator = new RecordGenerator(formClass)
                .distribution(REF_FIELD_ID, new RefGenerator(referencedForm));
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
        if (records == null) {
            this.records = generator.get(ROW_COUNT);
        }
        return records;
    }

    @Override
    public Supplier<FormInstance> getGenerator() {
        return generator;
    }

    public FormField getRefField() {
        return refField;
    }

    public TestForm getReferencedForm() {
        return referencedForm;
    }
}
