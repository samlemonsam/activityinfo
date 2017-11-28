package org.activityinfo.model.formTree;

import com.google.common.collect.Sets;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Created by yuriyz on 9/2/2016.
 */
public class ColumnNode {

    public static final String NON_BREAKING_SPACE = "\u00A0";

    private FormTree.Node node;
    private ExprNode expr;
    private String header;
    private ExprNode filter;

    public ColumnNode(FormTree.Node node) {
        this.node = node;
        this.header = composeHeader(node);
        if(node.isRoot()) {
            this.expr = new SymbolExpr(node.getFieldId());
        } else {
            this.expr = new CompoundExpr(
                    new SymbolExpr(node.getDefiningFormClass().getId()),
                        new SymbolExpr(node.getFieldId()));
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

    public ExprNode getExpr() {
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

    public ExprNode getFilter() {
        return filter;
    }

    public void setFilter(ExprNode filter) {
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
