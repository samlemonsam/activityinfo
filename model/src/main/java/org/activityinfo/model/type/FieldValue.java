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
package org.activityinfo.model.type;

import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

/**
 * Instance of a typed field value
 */
public interface FieldValue extends JsonSerializable {

    /**
     * The name of the field that contains the id of the {@code FieldTypeClass}
     * of this value
     */
    public static final String TYPE_CLASS_FIELD_NAME = "@type";

    /**
     *
     * @return this value's {@code FieldTypeClass}
     */
    FieldTypeClass getTypeClass();

    @Override
    JsonValue toJson();
}
