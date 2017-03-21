package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.i18n.shared.I18N;

public enum DateLevel {
    YEAR(I18N.CONSTANTS.year()),
    QUARTER(I18N.CONSTANTS.quarter()),
    MONTH(I18N.CONSTANTS.month()),
    DAY(I18N.CONSTANTS.dayOfMonth());

    private final String label;

    DateLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
