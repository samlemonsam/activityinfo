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
package org.activityinfo.model.job;

import org.activityinfo.json.JsonValue;

import static org.activityinfo.json.Json.createObject;

/**
 * Request body to start a new job
 */
public class JobRequest {
    private JobDescriptor descriptor;
    private String locale;

    public JobRequest(JobDescriptor descriptor, String locale) {
        this.descriptor = descriptor;
        this.locale = locale;
    }

    public JsonValue toJsonObject() {
        JsonValue object = createObject();
        object.put("type", descriptor.getType());
        object.put("descriptor", descriptor.toJson());
        if(locale != null) {
            object.put("locale", locale);
        }
        return object;
    }

    public JobDescriptor getDescriptor() {
        return descriptor;
    }

    public String getLocale() {
        return locale;
    }

    public static JobRequest fromJson(JsonValue object) {
        String type = object.get("type").asString();
        JobDescriptor descriptor = parseDescriptor(type, object.get("descriptor"));
        String locale = null;
        if(object.hasKey("locale")) {
            locale = object.get("locale").asString();
        }
        return new JobRequest(descriptor, locale);
    }

    public static JobDescriptor parseDescriptor(String type, JsonValue descriptor) {
        switch (type) {
            case ExportFormJob.TYPE:
                return ExportFormJob.fromJson(descriptor);
            case ExportAuditLog.TYPE:
                return ExportAuditLog.fromJson(descriptor);
            case ExportPivotTableJob.TYPE:
                return ExportPivotTableJob.fromJson(descriptor);
            case ExportLongFormatJob.TYPE:
                return ExportLongFormatJob.fromJson(descriptor);
            case ExportSitesJob.TYPE:
                return ExportSitesJob.fromJson(descriptor);
        }
        throw new IllegalArgumentException(type);
    }
}
