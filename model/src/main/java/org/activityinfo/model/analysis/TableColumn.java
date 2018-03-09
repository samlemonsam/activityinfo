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
package org.activityinfo.model.analysis;

import com.google.common.base.Optional;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;


@Value.Immutable
public abstract class TableColumn {

    @Value.Default
    public String getId() {
        return ResourceId.generateCuid();
    }

    public abstract Optional<String> getLabel();

    /**
     * @return the width of the column in pixels.
     */
    public abstract Optional<Integer> getWidth();

    public abstract String getFormula();

    @Value.Lazy
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("id", getId());
        if(getLabel().isPresent()) {
            object.put("label", getLabel().get());
        }
        if(getWidth().isPresent()) {
            object.put("width", getWidth().get());
        }
        object.put("formula", getFormula());
        return object;
    }

    public static TableColumn fromJson(JsonValue object) {
        ImmutableTableColumn.Builder tableColumn = ImmutableTableColumn.builder();
        tableColumn.id(object.getString("id"));
        if(object.hasKey("label")) {
            tableColumn.label(object.getString("label"));
        }
        if(object.hasKey("width")) {
            tableColumn.width((int)object.getNumber("width"));
        }
        tableColumn.formula(object.getString("formula"));
        return tableColumn.build();
    }
}
