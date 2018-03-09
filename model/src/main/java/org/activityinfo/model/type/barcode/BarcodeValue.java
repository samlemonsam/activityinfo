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
package org.activityinfo.model.type.barcode;

import com.google.common.base.Strings;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.JsonParsing;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.HasStringValue;

public class BarcodeValue implements FieldValue, HasStringValue {

    private final String code;

    public static BarcodeValue valueOf(String code) {
        if(Strings.isNullOrEmpty(code)) {
            return null;
        } else {
            return new BarcodeValue(code);
        }
    }

    private BarcodeValue(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String asString() {
        return code;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return BarcodeType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJson() {
        return Json.createFromNullable(code);
    }

    public static FieldValue valueOf(JsonValue element) {
        return BarcodeValue.valueOf(JsonParsing.toNullableString(element));
    }
}
