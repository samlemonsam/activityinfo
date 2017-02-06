package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormField;
import org.activityinfo.ui.client.icons.IconBundle;

/**
 * Created by alex on 2-2-17.
 */
public class FieldElement {

    private String id;
    private String label;
    private ExprNode expr;
    private ImageResource icon;

    public FieldElement(String id, String label, ExprNode expr, ImageResource icon) {
        this.id = id;
        this.label = label;
        this.icon = icon;
    }

    public static FieldElement count() {
        return new FieldElement("_count",
                "Count of all of records in the form", new ConstantExpr(1),
                IconBundle.INSTANCE.count());

    }


    public static FieldElement forField(FormField field) {
        return new FieldElement(
                field.getId().asString(),
                field.getLabel(),
                new SymbolExpr(field.getId()),
                IconBundle.iconForField(field.getType()));
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public ExprNode getExpr() {
        return expr;
    }

    public ImageResource getIcon() {
        return icon;
    }

}
