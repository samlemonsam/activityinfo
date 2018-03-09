package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.analysis.model.DimensionMapping;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.ImmutableDimensionModel;
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
    public DimensionModel dimensionModel() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label(form.getLabel() + " " + field.getLabel())
                .addMappings(new DimensionMapping(
                    new CompoundExpr(new SymbolNode(form.getId()), new SymbolNode(field.getId()))))
                .build();
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.iconForField(field.getType());
    }
}
