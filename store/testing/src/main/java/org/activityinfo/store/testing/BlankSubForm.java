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
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

public class BlankSubForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("BLANK_SUB_FORM");

    public static final int ROW_COUNT = IncidentForm.ROW_COUNT;

    private final GenericForm parentForm;
    private final FormClass formClass;

    private List<FormInstance> records = null;
    private RecordGenerator generator;

    public BlankSubForm(GenericForm parentForm) {
        this.parentForm = parentForm;
        formClass = new FormClass(FORM_ID);
        formClass.setLabel("Blank Sub-Form");
        formClass.setSubFormKind(SubFormKind.REPEATING);
        formClass.setParentFormId(parentForm.getFormId());

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
}
