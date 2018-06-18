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
import org.activityinfo.model.query.SortModel;
import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;

import java.util.List;

/**
 * The user's table model
 */
@Value.Immutable
public abstract class TableModel implements AnalysisModel  {

    private static final String TYPE = "table";

    public abstract ResourceId getFormId();

    public abstract List<TableColumn> getColumns();

    public abstract List<SortOrder> getOrdering();

    public abstract List<SortModel> getSorting();

    /**
     * A boolean-valued formula that determines which rows to include in the
     * results.
     */
    public abstract Optional<String> getFilter();

    @Value.Lazy
    @Override
    public String getTypeId() {
        return TYPE;
    }

    @Value.Lazy
    @Override
    public JsonValue toJson() {


        JsonValue object = Json.createObject();
        object.put("formId", getFormId().asString());

        if(getColumns().size() != 0) {
            JsonValue columnArray = Json.createArray();
            for (TableColumn tableColumn : getColumns()) {
                columnArray.add(tableColumn.toJson());
            }
            object.put("columns", columnArray);
        }
        if (getFilter().isPresent()) {
            object.put("filter", getFilter().get());
        }
        if (getSorting().size() != 0) {
            JsonValue columnArray = Json.createArray();
            for (SortModel sortModel : getSorting()) {
                columnArray.add(sortModel.toJson());
            }
            object.put("sort", columnArray);
        }

        return object;
    }

    public static TableModel fromJson(JsonValue object) {
        ImmutableTableModel.Builder model = ImmutableTableModel.builder();
        model.formId(ResourceId.valueOf(object.getString("formId")));

        if(object.hasKey("columns")) {
            JsonValue columnArray = object.get("columns");
            for (int i = 0; i < columnArray.length(); i++) {
                model.addColumns(TableColumn.fromJson(columnArray.get(i)));
            }
        }

        if (object.hasKey("filter")) {
            model.filter(Optional.of(object.getString("filter")));
        } else {
            model.filter(Optional.absent());
        }

        if (object.hasKey("sort")) {
            JsonValue columnArray = object.get("sort");
            for (int i = 0; i < columnArray.length(); i++) {
                model.addSorting(SortModel.fromJson(columnArray.get(i)));
            }
        }

        return model.build();
    }
}
