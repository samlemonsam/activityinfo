package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.i18n.shared.I18N;

import java.util.ArrayList;
import java.util.List;

/**
 * The way in which a measure is aggregate
 */
public enum Statistic {
    COUNT(I18N.CONSTANTS.count()),
    SUM(I18N.CONSTANTS.sum()),
    AVERAGE(I18N.CONSTANTS.average()),
    MEDIAN(I18N.CONSTANTS.median()),
    MIN(I18N.CONSTANTS.minimum()),
    MAX(I18N.CONSTANTS.maximum());

    private final String label;

    Statistic(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static List<String> labels() {
        List<String> labels = new ArrayList<>();
        for (Statistic statistic : values()) {
            labels.add(statistic.getLabel());
        }
        return labels;
    }
}
