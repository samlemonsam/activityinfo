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
package org.activityinfo.model.form;

import org.activityinfo.model.resource.ResourceId;

/**
 * Globally and uniquely identifies the named field of a given {@code FormClass}.
 * Distinguishes, for example, between the {@code label} field of a Province {@code FormClass}
 * and the {@code label} form on another {@code FormClass}.
 *
 * TODO: Properly split use of ResourceIds between real ResourceIds and FieldIds
 */
public class FieldId {

    public static ResourceId fieldId(ResourceId classId, String fieldName) {
        return ResourceId.valueOf(classId.asString() + "$" + fieldName);
    }

    public static ResourceId getFormClassId(ResourceId fieldId) {
        String qfn = fieldId.asString();
        int delimiter = qfn.indexOf('$');
        if(delimiter == -1) {
            throw new IllegalArgumentException("Not a fieldId: " + fieldId);
        }
        return ResourceId.valueOf(qfn.substring(0, delimiter));
    }

    public static String getFieldName(ResourceId fieldId) {
        String qfn = fieldId.asString();
        int delimiter = qfn.indexOf('$');
        if(delimiter == -1) {
            throw new IllegalArgumentException("Not a fieldId: " + fieldId);
        }
        return qfn.substring(delimiter+1);
    }
}
