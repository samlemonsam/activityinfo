package org.activityinfo.ui.client.component.table;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import org.activityinfo.model.formTree.ColumnNode;
import org.activityinfo.model.formTree.FieldPath;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Column that displays the value of a given field
 */
public class FieldColumn extends Column<RowView, String> {

    private ColumnNode column;

    public FieldColumn(ColumnNode column) {
        super(new TextCell());
        this.column = column;
    }

    public FieldColumn(FieldPath fieldPath, String header) {
        super(new TextCell());
        this.column = new ColumnNode(fieldPath, header);
    }

    public static List<FieldColumn> create(List<ColumnNode> columnNodes) {
        List<FieldColumn> columns = Lists.newArrayList();
        for (ColumnNode columnNode : columnNodes) {
            columns.add(new FieldColumn(columnNode));
        }
        return columns;
    }

    public ColumnNode get() {
        return column;
    }

    public Object getFieldValue(RowView rowView) {
        return rowView.getValue(column.getNode().getField().getId().asString());
    }

    @Override
    public String getValue(RowView rowView) {
        return column.getValue(getFieldValue(rowView));
    }

    @Override
    public String toString() {
        return "FieldColumn{" +
                "column='" + column +
                '}';
    }

    public static LinkedHashSet<String> headers(Collection<FieldColumn> columns) {
        LinkedHashSet<String> headers = Sets.newLinkedHashSet();
        for (FieldColumn column : columns) {
            headers.add(column.get().getHeader());
        }
        return headers;
    }
}
