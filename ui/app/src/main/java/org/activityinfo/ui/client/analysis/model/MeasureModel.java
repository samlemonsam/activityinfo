package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.observable.HasKey;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

/**
 * A measure contributes quantities to an analysis.
 */
public abstract class MeasureModel implements HasKey {

    private final String key;
    private final String label;

    public MeasureModel(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String getKey() {
        return key;
    }

    public abstract Observable<MeasureResultSet> compute(FormStore store);
}
