package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.analysis.model.DimensionSource;
import org.activityinfo.ui.client.analysis.model.FormDimensionSource;
import org.activityinfo.ui.client.icons.IconBundle;


public class FormNode extends DimensionNode {
    @Override
    public String getKey() {
        return "_form";
    }

    @Override
    public String getLabel() {
        return I18N.CONSTANTS.form();
    }

    @Override
    public DimensionSource dimensionModel() {
        return FormDimensionSource.INSTANCE;
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.INSTANCE.form();
    }
}
