package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
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
    public JsonObject toJson() {
        JsonObject object = Json.createObject();
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

        JsonArray mappingArray = Json.createArray();
        for (DimensionMapping mapping : getMappings()) {
            mappingArray.add(mapping.toJson());
        }

        object.put("mappings", mappingArray);

        return object;
    }

    public static DimensionModel fromJson(JsonObject object) {
        ImmutableDimensionModel.Builder model = ImmutableDimensionModel.builder();
        model.id(object.getString("id"));
        model.label(object.getString("label"));
        model.axis(Axis.valueOf(object.getString("axis").toUpperCase()));
        model.totals(object.getBoolean("totals"));
        model.percentage(object.getBoolean("percentage"));
        model.missingIncluded(object.getBoolean("missingIncluded"));

        if(object.hasKey("dateLevel")) {
            model.dateLevel(DateLevel.valueOf(object.getString("dateLevel")));
        }
        if(object.hasKey("totalLabel")) {
            model.totalLabel(Optional.of(object.getString("totalLabel")));
        }

        JsonArray mappings = object.getArray("mappings");
        for (int i = 0; i < mappings.length(); i++) {
            model.addMappings(DimensionMapping.fromJson(mappings.getObject(i)));
        }

        return model.build();
    }

}
