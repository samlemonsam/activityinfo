package org.activityinfo.model.formTree;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.model.expr.ExprNode;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by yuriyz on 9/2/2016.
 */
public class ColumnNode {

    public static final String NON_BREAKING_SPACE = "\u00A0";

    private FormTree.Node node;
    private List<FieldPath> fieldPaths;
    private String header;
    private ExprNode filter;

    public ColumnNode(FormTree.Node node) {
        this.node = node;
        this.header = composeHeader(node);
        this.fieldPaths = Lists.newArrayList(node.getPath());
    }

    public ColumnNode(FieldPath fieldPath, String header) {
        this.header = header;
        this.fieldPaths = Lists.newArrayList(fieldPath);
    }

    public String getValueAsString(Object value) {
        if (value != null) {
            return value.toString();
        }

        return NON_BREAKING_SPACE;
    }

    public void addFieldPath(FieldPath path) {
        fieldPaths.add(path);
    }

    public FormTree.Node getNode() {
        return node;
    }

    public List<FieldPath> getFieldPaths() {
        return fieldPaths;
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
