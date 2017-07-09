package org.activityinfo.ui.client.analysis.model;

import com.google.common.collect.ImmutableSet;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;

/**
 * Defines a pivot model from one or more data sources.
 */
@org.immutables.value.Value.Immutable
public abstract class AnalysisModel {

    public abstract List<MeasureModel> getMeasures();
    public abstract List<DimensionModel> getDimensions();

    @org.immutables.value.Value.Lazy
    public Set<ResourceId> formIds() {
        ImmutableSet.Builder<ResourceId> ids = ImmutableSet.builder();
        for (MeasureModel measure : getMeasures()) {
            ids.add(measure.getFormId());
        }
        return ids.build();
    }

    /**
     * Updates the model to update the given measure with the same id.
     */
    public AnalysisModel withMeasure(MeasureModel measureModel) {
        return ImmutableAnalysisModel.builder()
                .from(this)
                .measures(ImmutableLists.update(getMeasures(), measureModel, m -> m.getId()))
                .build();
    }

    public AnalysisModel withDimension(DimensionModel dimensionModel) {
        return ImmutableAnalysisModel.builder()
                .from(this)
                .dimensions(ImmutableLists.update(getDimensions(), dimensionModel, m -> m.getId()))
                .build();
    }

    public AnalysisModel withoutMeasure(String measureId) {
        return ImmutableAnalysisModel.builder()
                .from(this)
                .measures(ImmutableLists.remove(getMeasures(), measureId, m -> m.getId()))
                .build();
    }

    public AnalysisModel withoutDimension(String dimensionId) {
        return ImmutableAnalysisModel.builder()
                .from(this)
                .dimensions(ImmutableLists.remove(getDimensions(), dimensionId, d -> d.getId()))
                .build();
    }

    public AnalysisModel reorderDimensions(String afterId, List<DimensionModel> dims) {
        return ImmutableAnalysisModel.builder()
                .from(this)
                .dimensions(ImmutableLists.reorder(getDimensions(), afterId, dims, d -> d.getId()))
                .build();
    }

    /**
     *
     * @return true if any of the measures defined have multiple statistics.
     */
    @Value.Derived
    public boolean isMeasureDefinedWithMultipleStatistics() {
        for (MeasureModel measure : getMeasures()) {
            if(measure.getStatistics().size() > 1) {
                return true;
            }
        }
        for (DimensionModel dimensionModel : getDimensions()) {
            if(dimensionModel.getPercentage()) {
                return true;
            }
        }
        return false;
    }

    public JsonObject toJson() {

        JsonArray measures = Json.createArray();
        for (MeasureModel measureModel : getMeasures()) {
            measures.add(measureModel.toJson());
        }
        JsonArray dimensions = Json.createArray();
        for (DimensionModel dimensionModel : getDimensions()) {
            dimensions.add(dimensionModel.toJson());
        }

        JsonObject object = Json.createObject();
        object.put("measures", measures);
        object.put("dimensions", dimensions);
        return object;
    }

    public static AnalysisModel fromJson(JsonObject object) {
        ImmutableAnalysisModel.Builder model = ImmutableAnalysisModel.builder();
        JsonArray measures = object.getArray("measures");
        for (int i = 0; i < measures.length(); i++) {
            model.addMeasures(MeasureModel.fromJson(measures.getObject(i)));
        }
        JsonArray dimensions = object.getArray("dimensions");
        for (int i = 0; i < dimensions.length(); i++) {
            model.addDimensions(DimensionModel.fromJson(dimensions.getObject(i)));
        }

        return model.build();
    }
}
