package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.analysis.model.DimensionMapping;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.icons.IconBundle;

public class RootFieldNode extends DimensionNode {

    private FormField field;

    public RootFieldNode(FormField field) {
        this.field = field;
    }

    @Override
    public String getKey() {
        return "root:" + field.getId();
    }

    @Override
    public String getLabel() {
        return field.getLabel();
    }

    @Override
    public DimensionModel dimensionModel() {
        return new DimensionModel(
                ResourceId.generateCuid(),
                field.getLabel(),
                DimensionMapping.formMapping());
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.iconForField(field.getType());
    }
}