package org.activityinfo.model.type.subform;
/*
* #%L
* ActivityInfo Server
* %%
* Copyright (C) 2009 - 2013 UNICEF
* %%
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public
* License along with this program. If not, see
* <http://www.gnu.org/licenses/gpl-3.0.html>.
* #L%
*/

import com.google.common.base.Strings;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceIdPrefixType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ParametrizedFieldType;
import org.activityinfo.model.type.ParametrizedFieldTypeClass;
import org.activityinfo.model.type.RecordFieldTypeClass;
import org.activityinfo.model.type.number.Quantity;

/**
 * @author yuriyz on 12/03/2014.
 */
public class SubFormReferenceType implements ParametrizedFieldType {

    public static class TypeClass implements ParametrizedFieldTypeClass, RecordFieldTypeClass {

        private TypeClass() {
        }

        @Override
        public String getId() {
            return "SUBFORM";
        }

        @Override
        public SubFormReferenceType createType() {
            return new SubFormReferenceType();
        }

        @Override
        public SubFormReferenceType deserializeType(Record typeParameters) {
            String classId = typeParameters.isString("classReference");
            return new SubFormReferenceType()
                    .setClassId(Strings.isNullOrEmpty(classId) ? null : ResourceId.valueOf(classId));
        }

        @Override
        public FormClass getParameterFormClass() {
            return new FormClass(ResourceIdPrefixType.TYPE.id("subform"));
        }

        @Override
        public FieldValue deserialize(Record record) {
            return Quantity.fromRecord(record);
        }
    }

    public static final TypeClass TYPE_CLASS = new TypeClass();

    /**
     * Keeps reference to subformFormClass.
     */
    private ResourceId classId;

    public SubFormReferenceType() {
        this(null);
    }

    public SubFormReferenceType(ResourceId classId) {
        this.classId = classId;
    }

    public SubFormReferenceType setClassId(ResourceId classId) {
        this.classId = classId;
        return this;
    }

    public ResourceId getClassId() {
        return classId;
    }

    @Override
    public ParametrizedFieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public Record getParameters() {
        return new Record()
                .set("classId", getTypeClass().getParameterFormClass().getId())
                .set("classReference", classId != null ? classId.asString() : "");
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return "SubFormType";
    }
}