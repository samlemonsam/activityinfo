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
package org.activityinfo.model.formTree;

import com.google.common.collect.Sets;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Created by yuriyz on 9/2/2016.
 */
public class ColumnNode {

    public static final String NON_BREAKING_SPACE = "\u00A0";

    private FormTree.Node node;
    private FormulaNode expr;
    private String header;
    private FormulaNode filter;

    public ColumnNode(FormTree.Node node) {
        this.node = node;
        this.header = composeHeader(node);
        if(node.isRoot()) {
            this.expr = new SymbolNode(node.getFieldId());
        } else {
            this.expr = new CompoundExpr(
                    new SymbolNode(node.getDefiningFormClass().getId()),
                        new SymbolNode(node.getFieldId()));
        }
    }


    public String getValueAsString(Object value) {
        if (value != null) {
            return value.toString();
        }

        return NON_BREAKING_SPACE;
    }

    public FormTree.Node getNode() {
        return node;
    }

    public FormulaNode getExpr() {
        return expr;
    }

    public String getHeader() {
        return header;
    }

    private String composeHeader(FormTree.Node node) {
        if (node.getPath().isNested()) {
            return node.getDefiningFormClass().getLabel() + " " + node.getField().getLabel();
        } else {
            return node.getField().getLabel();
        }
    }

    public FormulaNode getFilter() {
        return filter;
    }

    public void setFilter(FormulaNode filter) {
        this.filter = filter;
    }

    public static LinkedHashSet<String> headers(Collection<ColumnNode> columns) {
        LinkedHashSet<String> headers = Sets.newLinkedHashSet();
        for (ColumnNode column : columns) {
            headers.add(column.getHeader());
        }
        return headers;
    }

    @Override
    public String toString() {
        return "ColumnNode{" +
                "header='" + header + '\'' +
                ", filter=" + filter +
                '}';
    }

}
