package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.form.FormField;
import org.activityinfo.ui.client.analysis.model.DimensionSourceModel;
import org.activityinfo.ui.client.analysis.model.FieldDimensionSource;
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
    public DimensionSourceModel dimensionModel() {
        return new FieldDimensionSource(field);
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.iconForField(field.getType());
    }
}