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
package org.activityinfo.model.query;

import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

import static org.activityinfo.json.Json.createObject;

public class RowSource implements JsonSerializable {

    private ResourceId rootFormId;

    public RowSource() {
    }

    public RowSource(ResourceId rootFormId) {
        this.rootFormId = rootFormId;
    }

    public ResourceId getRootFormId() {
        return rootFormId;
    }

    public RowSource setRootFormId(ResourceId rootFormId) {
        this.rootFormId = rootFormId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RowSource rowSource = (RowSource) o;

        if (rootFormId != null ? !rootFormId.equals(rowSource.rootFormId) : rowSource.rootFormId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return rootFormId != null ? rootFormId.hashCode() : 0;
    }


    @Override
    public JsonValue toJson() {
        JsonValue object = createObject();
        object.put("rootFormId", rootFormId.asString());
        return object;
    }

    public static RowSource fromJson(JsonValue object) {
        RowSource source = new RowSource();
        source.setRootFormId(ResourceId.valueOf(object.get("rootFormId").asString()));
        return source;
    }

}
