package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.analysis.model.FieldMeasure;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.icons.IconBundle;

/**
 * Meausure based on a quantity
 */
public class QuantityNode extends MeasureTreeNode {

    private ResourceId formId;
    private FormField field;

    public QuantityNode(ResourceId formId, FormField field) {
        this.formId = formId;
        this.field = field;
    }


    @Override
    public String getId() {
        return formId.asString() + "." + field.getId().asString();
    }

    @Override
    public String getLabel() {
        return field.getLabel();
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.iconForField(field.getType());
    }

    @Override
    public MeasureModel newMeasure() {
        return new FieldMeasure(ResourceId.generateCuid(), field.getLabel(), formId, new SymbolExpr(field.getId()));
    }
}
