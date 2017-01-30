package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.observable.HasKey;

/**
 * A measure contributes quantities to an analysis.
 */
public class MeasureModel implements HasKey {

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
}
