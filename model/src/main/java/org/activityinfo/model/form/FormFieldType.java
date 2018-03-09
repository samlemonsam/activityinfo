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

import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.TypeRegistry;

/**
 * The type of field, which influences how input is presented
 * the user, how it is validated, and what default measures
 * are available.
 */
public class FormFieldType {

    private FormFieldType() {}


    /**
     * Defined exact length of string to differ between FREE_TEXT and NARRATIVE types.
     * If string length less than #FREE_TEXT_LENGTH then type is #FREE_TEXT otherwise it is NARRATIVE.
     */
    public static final int FREE_TEXT_LENGTH = 80;


    public static FieldTypeClass valueOf(String name) {
        return TypeRegistry.get().getTypeClass(name);
    }

    public static FieldTypeClass[] values() {
        return new FieldTypeClass[] {
                FieldTypeClass.QUANTITY,
                FieldTypeClass.NARRATIVE,
                FieldTypeClass.FREE_TEXT,
                FieldTypeClass.LOCAL_DATE,
                FieldTypeClass.BOOLEAN,
                FieldTypeClass.GEOGRAPHIC_POINT,
                FieldTypeClass.BARCODE};

    }
}
