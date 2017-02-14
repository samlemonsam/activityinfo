package org.activityinfo.ui.client.formulaDialog;

import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.ui.client.icons.IconBundle;

/**
 * An element that can be added to the
 */
public class FormulaElement {

    public static final ModelKeyProvider<FormulaElement> KEY_PROVIDER = (element) -> element.getKey();

    public static final ValueProvider<FormulaElement, FormulaElement> VALUE_PROVIDER = new IdentityValueProvider<>();

    private final String key;
    private final String code;
    private final String label;
    private final ExprNode exprNode;
    private final ImageResource icon;

    public FormulaElement(String key, ExprNode exprNode, FormField field) {
        this.key = key;
        this.exprNode = exprNode;
        this.label = field.getLabel();
        this.code = field.getCode();
        this.icon = IconBundle.iconForField(field.getType());
    }

    public FormulaElement(FormulaElement field, EnumItem item) {
        this.key = field.getKey() + "#" + item.getId().asString();
        this.exprNode = new ConstantExpr(item.getLabel());
        this.label = item.getLabel();
        this.icon = IconBundle.INSTANCE.enumField();
        this.code = null;
    }


    public String getKey() {
        return key;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean hasCode() {
        return code != null;
    }

    public static FormulaElement fieldNode(FormTree.Node node) {
        return new FormulaElement("field:" + node.getPath().toString(), exprFor(node), node.getField());
    }

    private static ExprNode exprFor(FormTree.Node node) {
        SymbolExpr fieldSymbol = new SymbolExpr(node.getField().hasCode() ? node.getField().getCode() : node.getFieldId().asString());
        if(node.isRoot()) {
            return fieldSymbol;
        } else {
            return new CompoundExpr(exprFor(node.getParent()), fieldSymbol);
        }
    }

    public ImageResource getIcon() {
        return icon;
    }

    public boolean matches(String filter) {
        String filterLowered = filter.toLowerCase();
        return (code != null && code.toLowerCase().contains(filterLowered)) ||
                label.toLowerCase().contains(filter);
    }

    public ExprNode getExpr() {
       return exprNode;
    }

}
