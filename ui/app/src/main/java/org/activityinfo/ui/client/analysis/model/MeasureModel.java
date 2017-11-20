package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;
import org.immutables.value.Value;

import java.util.Collections;
import java.util.Set;

/**
 * A measure contributes quantities to an analysis.
 */
@org.immutables.value.Value.Immutable
public abstract class MeasureModel {

    @org.immutables.value.Value.Default
    public String getId() {
        return ResourceId.generateCuid();
    }

    public abstract String getLabel();

    /**
     *
     * @return the id of the form that is the source of this measure.
     */
    public abstract ResourceId getFormId();


    public abstract String getFormula();

    /**
     *
     * @return the statistics function used to combine values.
     */
    @org.immutables.value.Value.Default
    public Set<Statistic> getStatistics() {
        return Collections.singleton(Statistic.SUM);
    }

    @Value.Lazy
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("id", getId());
        object.put("label", getLabel());
        object.put("formId", getFormId().asString());
        object.put("formula", getFormula());

        JsonValue statArray = Json.createArray();
        for (Statistic statistic : getStatistics()) {
            statArray.add(Json.create(statistic.name()));
        }
        object.put("statistics", statArray);
        return object;
    }

    public static MeasureModel fromJson(JsonValue object) {
        ImmutableMeasureModel.Builder model = ImmutableMeasureModel.builder()
            .id(object.getString("id"))
            .label(object.getString("label"))
            .formId(ResourceId.valueOf(object.getString("formId")))
            .formula(object.getString("formula"));

        JsonValue statArray = object.get("statistics");
        for (int i = 0; i < statArray.length(); i++) {
            model.addStatistics(Statistic.valueOf(statArray.get(i).asString().toUpperCase()));
        }
        return model.build();
    }
}
