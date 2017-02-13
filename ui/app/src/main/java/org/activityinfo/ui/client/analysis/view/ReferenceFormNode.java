package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.analysis.model.DimensionSourceModel;
import org.activityinfo.ui.client.icons.IconBundle;


public class ReferenceFormNode extends DimensionNode {

    private FormClass formClass;
    private final DimensionNode firstChild;

    public ReferenceFormNode(FormClass formClass, DimensionNode firstChild) {
        this.formClass = formClass;
        this.firstChild = firstChild;
    }


    @Override
    public String getKey() {
        return "_ref:" + formClass.getId();
    }

    @Override
    public String getLabel() {
        return formClass.getLabel();
    }

    @Override
    public DimensionSourceModel dimensionModel() {
        return firstChild.dimensionModel();
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.INSTANCE.referenceField();
    }
}
