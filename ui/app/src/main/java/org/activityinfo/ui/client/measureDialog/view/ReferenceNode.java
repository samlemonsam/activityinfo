package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.icons.IconBundle;

public class ReferenceNode extends MeasureTreeNode {

    private FormTree.Node node;

    public ReferenceNode(FormTree.Node node) {
        this.node = node;
    }

    @Override
    public String getId() {
        return node.getPath().toString();
    }

    @Override
    public String getLabel() {
        return node.getField().getLabel();
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.iconForField(node.getType());
    }

    @Override
    public MeasureModel newMeasure() {
        throw new UnsupportedOperationException();
    }
}
