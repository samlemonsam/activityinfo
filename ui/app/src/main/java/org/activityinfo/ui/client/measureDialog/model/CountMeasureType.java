package org.activityinfo.ui.client.measureDialog.model;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.analysis.model.CountMeasure;

public class CountMeasureType implements MeasureType {
    @Override
    public String getId() {
        return "count";
    }

    @Override
    public String getLabel() {
        return "Count";
    }

    @Override
    public CountMeasure buildModel(FormClass formClass) {
        return new CountMeasure(formClass.getId(), I18N.MESSAGES.countMeasure(formClass.getLabel()));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CountMeasureType;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
