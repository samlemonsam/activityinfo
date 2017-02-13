package org.activityinfo.ui.client.formulaDialog;

import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.ui.client.icons.IconBundle;

/**
 * An element that can be added to the
 */
public class FormulaElement {

    public static final ModelKeyProvider<FormulaElement> KEY_PROVIDER = (element) -> element.getKey();

    public static final ValueProvider<FormulaElement, FormulaElement> VALUE_PROVIDER = new IdentityValueProvider<>();

    private String key;
    private String code;
    private String label;
    private ExprNode exprNode;
    private ImageResource icon;

    public FormulaElement(String key, ExprNode exprNode, FormField field) {
        this.key = key;
        this.exprNode = exprNode;
        this.label = field.getLabel();
        this.code = field.getCode();
        this.icon = IconBundle.iconForField(field.getType());
    }

    private FormulaElement(String key, String label) {
        this.key = key;
        this.label = label;
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

    public static FormulaElement folder(String id, String label) {
        return new FormulaElement(id, label);
    }

    public static FormulaElement fieldNode(FormTree.Node node) {
        return new FormulaElement("field:" + node.getPath().toString(), node.getPath().toExpr(), node.getField());
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
        if(code != null) {
            return new SymbolExpr(code);
        } else {
            return new SymbolExpr(label);
        }
    }
}
