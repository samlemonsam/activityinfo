/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.formulaDialog;

import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
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
    private final FormulaNode formulaNode;
    private final ImageResource icon;

    public FormulaElement(String key, FormulaNode formulaNode, FormField field) {
        this.key = key;
        this.formulaNode = formulaNode;
        this.label = field.getLabel();
        this.code = field.getCode();
        this.icon = IconBundle.iconForField(field.getType());
    }

    public FormulaElement(FormulaElement field, EnumItem item) {
        this.key = field.getKey() + "#" + item.getId().asString();
        this.formulaNode = new CompoundExpr(field.getExpr(), new SymbolNode(symbolFor(item)));
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

    private static FormulaNode exprFor(FormTree.Node node) {
        SymbolNode fieldSymbol = new SymbolNode(symbolFor(node.getField()));
        if(node.isRoot()) {
            return fieldSymbol;
        } else {
            return new CompoundExpr(exprFor(node.getParent()), fieldSymbol);
        }
    }

    private static String symbolFor(FormField field) {
        if(field.hasCode()) {
            return field.getCode();
        } else if(isSuitableSymbol(field.getLabel())) {
            return field.getLabel();
        } else {
            return field.getId().asString();
        }
    }


    private String symbolFor(EnumItem item) {
        if(isSuitableSymbol(item.getLabel())) {
            return item.getLabel();
        } else {
            return item.getId().asString();
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

    public FormulaNode getExpr() {
       return formulaNode;
    }

    public static boolean isSuitableSymbol(String text) {
        return text.matches("^[A-Za-z][A-Za-z0-9_#,:\\s]{1,20}$");
    }

}
