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
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;

import java.util.List;

public class LocationSelectionForm implements TestForm {

    private final FormClass formClass;
    private final FormField localiteField;
    private final RecordGenerator recordGenerator;
    private final LazyRecordList records;

    public LocationSelectionForm(Ids ids, LocaliteForm localiteForm) {
        formClass = new FormClass(ids.formId("LOC"));
        formClass.setLabel("Location Selection");
        formClass.setDatabaseId(ids.databaseId());

        for (FormField field : ids.builtinFields()) {
            formClass.addElement(field);
        }

        localiteField = formClass.addField(ids.fieldId("LOCATION"))
                .setLabel("Localite")
                .setType(new ReferenceType(Cardinality.SINGLE, localiteForm.getFormId()))
                .setRequired(true)
                .setVisible(true);

        recordGenerator = new RecordGenerator(formClass)
                .distribution(localiteField.getId(), new RefGenerator(localiteForm));
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
    public List<FormInstance> getRecords() {
        return records.get();
    }

    @Override
    public Supplier<FormInstance> getGenerator() {
        return recordGenerator;
    }

    public FormField getLocalitieField() {
        return localiteField;
    }
}