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
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.List;

/**
 * Test data set for the classic "Non Food Items" (NFI) distribution use case
 */
public class NfiForm implements TestForm {


    private final FormClass formClass;
    private final FormField dateField;
    private final FormField villageField;
    private final RecordGenerator recordGenerator;
    private final LazyRecordList records;

    public NfiForm(Ids ids, VillageForm villageForm) {
        formClass = new FormClass(ids.formId("NFI"));
        formClass.setLabel("NFI Distribution");
        formClass.setDatabaseId(ids.databaseId());

        for (FormField field : ids.builtinFields()) {
            formClass.addElement(field);
        }

        dateField = formClass.addField(ids.fieldId("F1"))
            .setLabel("Date of distribution")
            .setType(LocalDateType.INSTANCE)
            .setRequired(true)
            .setVisible(true);

        villageField = formClass.addField(ids.fieldId("F2"))
            .setLabel("Village")
            .setType(new ReferenceType(Cardinality.SINGLE, villageForm.getFormId()))
            .setRequired(true)
            .setVisible(true);

        recordGenerator = new RecordGenerator(formClass)
                .distribution(villageField.getId(), new RefGenerator(villageForm));
        records = new LazyRecordList(recordGenerator, 821);
    }

    public RecordRef getRecordRef(int index) {
        return getRecords().get(index).getRef();
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

    public FormField getDateField() {
        return dateField;
    }

    public FormField getVillageField() {
        return villageField;
    }
}
