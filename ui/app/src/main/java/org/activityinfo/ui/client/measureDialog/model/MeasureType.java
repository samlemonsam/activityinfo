package org.activityinfo.ui.client.measureDialog.model;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.analysis.model.CountMeasure;

public interface MeasureType {

    String getId();

    String getLabel();

    CountMeasure buildModel(FormClass formClass);
}
