package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.observable.HasKey;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

/**
 * A measure contributes quantities to an analysis.
 */
public abstract class MeasureModel implements HasKey {

    private final String id;
    private final String label;

    public MeasureModel(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String getKey() {
        return id;
    }

    /**
     * Computes the list of available dimension sources for this measure.
     */
    public abstract Observable<FormForest> getFormSet(FormStore store);

    /**
     * Computes the values for this measure.
     */
    public abstract Observable<MeasureResultSet> compute(FormStore store, Observable<DimensionSet> dimensions);

}
