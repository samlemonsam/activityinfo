package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.icons.IconBundle;

/**
 * Groups together fields that belong to a form
 */
public class FormNode extends MeasureTreeNode {

    private FormClass formClass;

    public FormNode(FormClass formClass) {
        this.formClass = formClass;
    }

    @Override
    public String getId() {
        return formClass.getId().asString();
    }

    @Override
    public String getLabel() {
        return formClass.getLabel();
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.INSTANCE.form();
    }

    @Override
    public MeasureModel newMeasure() {
        throw new UnsupportedOperationException();
    }
}
