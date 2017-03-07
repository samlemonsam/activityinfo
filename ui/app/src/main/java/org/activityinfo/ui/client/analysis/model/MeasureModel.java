package org.activityinfo.ui.client.analysis.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.observable.HasKey;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.ui.client.store.FormStore;

/**
 * A measure contributes quantities to an analysis.
 */
public final class MeasureModel implements HasKey {

    private final String id;
    private final MeasureSource source;
    private final StatefulValue<String> label = new StatefulValue<>();

    public MeasureModel(String id, String label, MeasureSource source) {
        this.id = id;
        this.source = source;
        this.label.updateValue(label);
    }

    public Observable<String> getLabel() {
        return label;
    }

    public Observable<MeasureLabels> getLabels() {
        return label.transform(label -> new MeasureLabels(label));
    }

    @Override
    public String getKey() {
        return id;
    }

    public MeasureSource getSource() {
        return source;
    }

    /**
     * Computes the list of available dimension sources for this measure.
     */
    public Observable<FormForest> getFormSet(FormStore store) {
        return source.getFormSet(store);
    }

    /**
     * Computes the values for this measure.
     */
    public Observable<MeasureResultSet> compute(FormStore store, Observable<DimensionSet> dimensions) {
        return source.compute(store, dimensions, getLabels());
    }


    public JsonElement toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("label", label.get());
        return object;
    }

    void updateFormula(String formula) {

    }

    public void updateLabel(String value) {
        this.label.updateIfNotEqual(value);
    }
}
