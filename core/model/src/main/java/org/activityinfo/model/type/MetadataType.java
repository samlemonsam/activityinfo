package org.activityinfo.model.type;
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Maps;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceIdPrefixType;

import java.io.Serializable;
import java.util.HashMap;

/**
 * It's not really type but convenient way to keep metadata of formclass as formfield (typically invisible).
 * Use cases:
 *  - label representation on FormDesigner
 *
 * @author yuriyz on 03/28/2016.
 */
public class MetadataType implements ParametrizedFieldType {

    public static class TypeClass implements ParametrizedFieldTypeClass, Serializable {

        private TypeClass() {}

        @Override
        public String getId() {
            return "METADATA_TYPE";
        }

        @Override
        public MetadataType createType() {
            return new MetadataType();
        }

        @Override
        public MetadataType deserializeType(Record typeParameters) {
            MetadataType type = new MetadataType();
            type.getValues().putAll(typeParameters.getRecord("values").getProperties());
            return type;
        }

        @Override
        public FormClass getParameterFormClass() {
            FormClass formClass = new FormClass(ResourceIdPrefixType.TYPE.id("metadata"));
            formClass.addElement(new FormField(ResourceId.valueOf("text"))
                    .setType(FREE_TEXT.createType())
                    .setLabel(I18N.CONSTANTS.text()));
            return formClass;
        }
    }

    public static final TypeClass TYPE_CLASS = new TypeClass();

    private HashMap<String, Object> values = Maps.newHashMap();

    public MetadataType() {
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    @Override
    public ParametrizedFieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public Record getParameters() {
        return new Record()
                .set("values", new Record().setAll(values))
                .set("classId", getTypeClass().getParameterFormClass().getId());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return "MetadataType{" +
                "values='" + values + '\'' +
                '}';
    }
}
