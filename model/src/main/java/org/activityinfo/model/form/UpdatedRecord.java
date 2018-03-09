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

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.JsonValue;


@JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
public final class UpdatedRecord {

    private String recordId;
    private String parentRecordId;
    private JsonValue fields;

    public UpdatedRecord() {
    }

    @JsOverlay
    public static UpdatedRecord create(FormRecord record) {
        UpdatedRecord update = new UpdatedRecord();
        update.parentRecordId = record.getParentRecordId();
        update.recordId = record.getRecordId();
        update.fields = record.getFields();
        return update;
    }

    @JsOverlay
    public String getRecordId() {
        return recordId;
    }

    @JsOverlay
    public String getParentRecordId() {
        return parentRecordId;
    }

    @JsOverlay
    public JsonValue getFields() {
        return fields;
    }
}
