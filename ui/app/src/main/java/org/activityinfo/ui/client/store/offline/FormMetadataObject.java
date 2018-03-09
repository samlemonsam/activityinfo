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
package org.activityinfo.ui.client.store.offline;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormPermissions;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class FormMetadataObject {

    private String version;
    private JsonValue permissions;

    public FormMetadataObject() {
    }

    @JsOverlay
    public static FormMetadataObject from(FormMetadata metadata) {
        FormMetadataObject object = new FormMetadataObject();
        object.version = Long.toString(metadata.getVersion());
        object.permissions = metadata.getPermissions().toJson();
        return object;
    }

    @JsOverlay
    public long getVersion() {
        return Long.parseLong(version);
    }


    @JsOverlay
    public FormPermissions getPermissions() {
        return FormPermissions.fromJson(permissions);
    }


}
