package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.analysis.model.DimensionMapping;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
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
    public DimensionModel dimensionModel() {
        return new DimensionModel(ResourceId.generateCuid(), I18N.CONSTANTS.form(), DimensionMapping.formMapping());
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.INSTANCE.form();
    }
}
