package org.activityinfo.ui.client.component.table;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Column that displays the value of a given field
 */
public class FieldColumn extends Column<RowView, String> {

    public static final String NON_BREAKING_SPACE = "\u00A0";

    private FormTree.Node node;
    private List<FieldPath> fieldPaths;
    private String header;
    private ExprNode filter;

    public FieldColumn(FormTree.Node node) {
        super(new TextCell());
        this.node = node;
        this.header = composeHeader(node);
        this.fieldPaths = Lists.newArrayList(node.getPath());
    }

    public FieldColumn(FieldPath fieldPath, String header) {
        super(new TextCell());
        this.header = header;
        this.fieldPaths = Lists.newArrayList(fieldPath);
    }

    public Object getFieldValue(RowView rowView) {
        return rowView.getValue(node.getField().getId().asString());
    }

    @Override
    public String getValue(RowView rowView) {
        Object value = getFieldValue(rowView);
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

    public static LinkedHashSet<String> headers(Collection<FieldColumn> columns) {
        LinkedHashSet<String> headers = Sets.newLinkedHashSet();
        for (FieldColumn column : columns) {
            headers.add(column.getHeader());
        }
        return headers;
    }

    @Override
    public String toString() {
        return "FieldColumn{" +
                "header='" + header + '\'' +
                ", filter=" + filter +
                '}';
    }
}
