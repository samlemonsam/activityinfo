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
package org.activityinfo.model.formTree;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;

/**
 * Test Form Classes
 */
class Forms implements FormClassProvider {
    final FormClass student;
    final FormClass school;
    final FormClass village;
    final FormClassProvider formClassProvider;

    public Forms() {
        village = form("village");
        village.addElement(textField("name"));
        village.addElement(textField("code"));

        school = form("school");
        school.addElement(textField("name"));
        school.addElement(textField("schoolId"));
        school.addElement(referenceField("village", village));

        student = form("student");
        student.addElement(textField("name"));
        student.addElement(textField("studentId"));
        student.addElement(referenceField("school", school));

        formClassProvider = FormClassProviders.of(village, school, student);
    }

    private FormClass form(String name) {
        return new FormClass(ResourceId.valueOf(name))
                .setLabel(name);
    }

    private FormField textField(String label) {
        return new FormField(ResourceId.valueOf(label))
                .setType(TextType.SIMPLE)
                .setLabel(label);
    }

    private FormField referenceField(String label, FormClass formClass) {
        return  new FormField(ResourceId.valueOf(label))
                .setType(ReferenceType.single(formClass.getId()))
                .setLabel(label);
    }

    @Override
    public FormClass getFormClass(ResourceId formId) {
        return formClassProvider.getFormClass(formId);
    }

    public FormClass getStudent() {
        return student;
    }

    public FormClass getSchool() {
        return school;
    }

    public FormClass getVillage() {
        return village;
    }

}
