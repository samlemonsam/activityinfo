package org.activityinfo.ui.client.analysis.model;

import com.google.common.collect.ImmutableSet;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.analysis.AnalysisModel;
import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;

/**
 * Defines a pivot model from one or more data sources.
 */
@org.immutables.value.Value.Immutable
public abstract class PivotModel implements AnalysisModel {

    public static final String TYPE = "pivot";

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
    public PivotModel withMeasure(MeasureModel measureModel) {
        return ImmutablePivotModel.builder()
                .from(this)
                .measures(ImmutableLists.update(getMeasures(), measureModel, m -> m.getId()))
                .build();
    }

    public PivotModel withDimension(DimensionModel dimensionModel) {
        return ImmutablePivotModel.builder()
                .from(this)
                .dimensions(ImmutableLists.update(getDimensions(), dimensionModel, m -> m.getId()))
                .build();
    }

    public PivotModel withoutMeasure(String measureId) {
        return ImmutablePivotModel.builder()
                .from(this)
                .measures(ImmutableLists.remove(getMeasures(), measureId, m -> m.getId()))
                .build();
    }

    public PivotModel withoutDimension(String dimensionId) {
        return ImmutablePivotModel.builder()
                .from(this)
                .dimensions(ImmutableLists.remove(getDimensions(), dimensionId, d -> d.getId()))
                .build();
    }

    public PivotModel reorderDimensions(String afterId, List<DimensionModel> dims) {
        return ImmutablePivotModel.builder()
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

    @Override
    @Value.Lazy
    public String getTypeId() {
        return TYPE;
    }

    @Value.Lazy
    @Override
    public JsonValue toJson() {

        JsonValue measures = Json.createArray();
        for (MeasureModel measureModel : getMeasures()) {
            measures.add(measureModel.toJson());
        }
        JsonValue dimensions = Json.createArray();
        for (DimensionModel dimensionModel : getDimensions()) {
            dimensions.add(dimensionModel.toJson());
        }

        JsonValue object = Json.createObject();
        object.put("measures", measures);
        object.put("dimensions", dimensions);
        return object;
    }

    public static PivotModel fromJson(JsonValue jsonObject) {

        ImmutablePivotModel.Builder model = ImmutablePivotModel.builder();

        JsonValue measures = jsonObject.get("measures");
        for (int i = 0; i < measures.length(); i++) {
            model.addMeasures(MeasureModel.fromJson(measures.get(i)));
        }
        JsonValue dimensions = jsonObject.get("dimensions");
        for (int i = 0; i < dimensions.length(); i++) {
            model.addDimensions(DimensionModel.fromJson(dimensions.get(i)));
        }

        return model.build();
    }

}
