package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.ui.client.analysis.model.MeasureModel;


public abstract class MeasureTreeNode {

    public abstract String getId();

    public abstract String getLabel();

    public abstract ImageResource getIcon();

    public abstract  MeasureModel newMeasure();
}
