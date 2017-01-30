package org.activityinfo.ui.client.measureDialog.model;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.analysis.model.MeasureModel;

public interface MeasureType {

    String getId();

    String getLabel();

    MeasureModel buildModel(FormClass formClass);
}
