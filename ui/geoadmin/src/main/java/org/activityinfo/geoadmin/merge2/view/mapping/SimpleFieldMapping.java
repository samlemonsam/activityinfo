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
package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;

/**
 * Maps a root field directly to another root field.
 * 
 */
public class SimpleFieldMapping implements FieldMapping {

    private FieldProfile sourceField;
    private FieldProfile targetField;

    public SimpleFieldMapping(FieldProfile sourceField, FieldProfile targetField) {
        this.targetField = targetField;
        this.sourceField = sourceField;
    }

    public static boolean isSimple(FieldProfile targetField) {
        FieldType fieldType = targetField.getFormField().getType();
        return fieldType instanceof TextType;
    }

    @Override
    public ResourceId getTargetFieldId() {
        return targetField.getId();
    }

    @Override
    public FieldValue mapFieldValue(int sourceIndex) {
        FieldType type = targetField.getFormField().getType();
        if(type instanceof TextType) {
            return TextValue.valueOf(sourceField.getView().getString(sourceIndex));
        } else {
            throw new UnsupportedOperationException("target type: "  + type);
        }
    }
}
