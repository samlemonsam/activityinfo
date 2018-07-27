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
package org.activityinfo.model.analysis.pivot;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

/**
 * A Dimension has a number of discrete categories into which measures are disaggregated.
 *
 * <p>Importantly, a dimension must break up quantitative data from multiple data sources into common
 * categories. </p>
 */
@org.immutables.value.Value.Immutable
public abstract class DimensionModel {

    public static final String STATISTIC_ID = "statistic";

    public static final String MEASURE_ID = "measure";

    public abstract String getId();
    public abstract String getLabel();
    public abstract List<DimensionMapping> getMappings();

    @org.immutables.value.Value.Default
    public Axis getAxis() {
        return Axis.ROW;
    }

    @org.immutables.value.Value.Default
    public boolean getTotals() {
        return false;
    }

    @org.immutables.value.Value.Default
    public boolean getMissingIncluded() { return true; }

    public abstract Optional<DateLevel> getDateLevel();

    public abstract Optional<String> getTotalLabel();

    public abstract Optional<String> getMissingLabel();

    @org.immutables.value.Value.Default
    public boolean getPercentage() {
        return false;
    }

    @Value.Lazy
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("id", getId());
        object.put("label", getLabel());
        object.put("axis", getAxis().name());
        object.put("totals", getTotals());
        object.put("percentage", getPercentage());
        object.put("missingIncluded", getMissingIncluded());

        if(getDateLevel().isPresent()) {
            object.put("dateLevel", getDateLevel().get().name());
        }
        if(getTotalLabel().isPresent()) {
            object.put("totalLabel", getTotalLabel().get());
        }

        JsonValue mappingArray = Json.createArray();
        for (DimensionMapping mapping : getMappings()) {
            mappingArray.add(mapping.toJson());
        }

        object.put("mappings", mappingArray);

        return object;
    }

    public static DimensionModel fromJson(JsonValue object) {
        ImmutableDimensionModel.Builder model = ImmutableDimensionModel.builder();
        model.id(object.getString("id"));
        model.label(object.getString("label"));
        model.axis(Axis.valueOf(object.getString("axis").toUpperCase()));
        model.totals(object.getBoolean("totals"));
        model.percentage(object.getBoolean("percentage"));
        model.missingIncluded(object.getBoolean("missingIncluded"));

        if(object.hasKey("dateLevel") && object.get("dateLevel").getBoolean("present")) {
            model.dateLevel(DateLevel.valueOf(object.getString("dateLevel")));
        }
        if(object.hasKey("totalLabel")) {
            model.totalLabel(Optional.of(object.getString("totalLabel")));
        }

        JsonValue mappings = object.get("mappings");
        for (int i = 0; i < mappings.length(); i++) {
            model.addMappings(DimensionMapping.fromJson(mappings.get(i)));
        }

        return model.build();
    }

}
