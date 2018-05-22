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

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.activityinfo.json.Json.createObject;

/**
 * Status information the current offline snapshot.
 */
public class SnapshotStatus {

    public static final SnapshotStatus EMPTY = new SnapshotStatus();

    private Date time = new Date();
    private Map<ResourceId, Long> formVersions = new HashMap<>();

    private SnapshotStatus() {
    }

    public SnapshotStatus(SnapshotDelta snapshot) {
        for (FormMetadata metadata : snapshot.getForms()) {
            formVersions.put(metadata.getId(), metadata.getVersion());
        }
    }

    public boolean isEmpty() {
        return formVersions.isEmpty();
    }

    public Date getSnapshotTime() {
        return time;
    }

    public boolean isFormCached(ResourceId formId) {
        return formVersions.containsKey(formId);
    }

    public long getLocalVersion(ResourceId formId) {
        return formVersions.getOrDefault(formId, 0L);
    }

    public JsonValue toJson() {
        JsonValue versions = createObject();
        for (Map.Entry<ResourceId, Long> entry : formVersions.entrySet()) {
            versions.put(entry.getKey().asString(), Long.toString(entry.getValue()));
        }

        JsonValue object = createObject();
        object.put("time", time.getTime());
        object.put("versions", versions);
        return object;
    }

    public static SnapshotStatus fromJson(JsonValue object) {
        SnapshotStatus status = new SnapshotStatus();
        status.time = new Date(object.get("time").asLong());

        JsonValue versions = object.get("versions");
        String[] forms = versions.keys();

        for (String form : forms) {
            ResourceId formId = ResourceId.valueOf(form);
            long version = Long.parseLong(versions.getString(form));

            status.formVersions.put(formId, version);
        }

        return status;
    }

    @Override
    public String toString() {
        return "SnapshotStatus{" +
                "time=" + time +
                ", formVersions=" + formVersions +
                '}';
    }

    public boolean areAllCached(Set<ResourceId> formIds) {
        for (ResourceId formId : formIds) {
            if(!isFormCached(formId)) {
                return false;
            }
        }
        return true;
    }
}
