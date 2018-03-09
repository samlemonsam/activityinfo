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

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.List;

public class AdminLevelForm implements TestForm {

    private final FormClass formClass;
    private final FormField nameField;
    private final RecordGenerator recordGenerator;
    private final LazyRecordList records;
    private final FormField parentField;
    private String levelName;
    private int count;
    private Optional<AdminLevelForm> parent;

    public AdminLevelForm(Ids ids, String name, int count, Optional<AdminLevelForm> parent) {
        levelName = name;
        this.count = count;
        this.parent = parent;

        formClass = new FormClass(ids.formId(name.toUpperCase()));
        formClass.setLabel(name);
        formClass.setDatabaseId(ids.databaseId());

        for (FormField field : ids.builtinFields()) {
            formClass.addElement(field);
        }

        if(parent.isPresent()) {
            parentField = formClass.addField(ids.fieldId("PARENT"))
                .setLabel(parent.get().getFormClass().getLabel())
                .setType(new ReferenceType(Cardinality.SINGLE, parent.get().getFormId()))
                .setRequired(true)
                .setKey(true)
                .setVisible(true);
        } else {
            parentField = null;
        }

        nameField = formClass.addField(ids.fieldId("F1"))
            .setLabel("Name")
            .setType(TextType.SIMPLE)
            .setRequired(true)
            .setKey(true)
            .setVisible(true);


        recordGenerator = new RecordGenerator(formClass);
        recordGenerator.distribution(nameField.getId(), new UniqueNameGenerator(levelName));

        if(parent.isPresent()) {
            recordGenerator.distribution(parentField.getId(), new RefGenerator(parent.get()));
        }

        records = new LazyRecordList(recordGenerator, count);
    }

    public int getCount() {
        return count;
    }

    public Optional<AdminLevelForm> getParentForm() {
        return parent;
    }

    public ResourceId getParentFieldId() {
        assert parentField != null : "AdminLevel has no parent";
        return parentField.getId();
    }

    public RecordRef getRecordRef(int index) {
        return getRecords().get(index).getRef();
    }

    public ResourceId getNameFieldId() {
        return nameField.getId();
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

}
