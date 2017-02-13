package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.ui.client.analysis.model.DimensionSourceModel;
import org.activityinfo.ui.client.analysis.model.FieldDimensionSource;
import org.activityinfo.ui.client.icons.IconBundle;


public class ReferencedNode extends DimensionNode {

    private FormClass form;
    private FormField field;

    public ReferencedNode(FormClass form, FormField field) {
        this.form = form;
        this.field = field;
    }

    @Override
    public String getKey() {
        return "_ref:" + form.getId().asString() + "." + field.getId();
    }

    @Override
    public String getLabel() {
        return field.getLabel();
    }

    @Override
    public DimensionSourceModel dimensionModel() {
        return new FieldDimensionSource(form.getLabel() + " " + field.getLabel(),
                new CompoundExpr(new SymbolExpr(form.getId()), new SymbolExpr(field.getId())));
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.iconForField(field.getType());
    }
}
