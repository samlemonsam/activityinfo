package org.activityinfo.ui.client.measureDialog.model;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.analysis.model.CountMeasure;

import java.util.Optional;

public interface MeasureType {

    String getId();

    String getLabel();

    CountMeasure buildModel(FormClass formClass);

}
