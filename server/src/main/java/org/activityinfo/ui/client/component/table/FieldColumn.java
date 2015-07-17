package org.activityinfo.ui.client.component.table;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;

import java.util.List;
import java.util.Set;

/**
 * Column that displays the value of a given field
 */
public class FieldColumn extends Column<Projection, String> {

    public static final String NON_BREAKING_SPACE = "\u00A0";

    private FormTree.Node node;
    private List<FieldPath> fieldPaths;
    private String header;
    private Criteria criteria;

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

    public FieldValue getFieldValue(Projection projection) {
        for (FieldPath path : fieldPaths) {
            final FieldValue value = projection.getValue(path);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String getValue(Projection projection) {
        final FieldValue fieldValue = getFieldValue(projection);

        if (fieldValue instanceof Quantity) {
            Double value = ((Quantity) fieldValue).getValue();
            return value.toString();

        } else if (fieldValue instanceof EnumValue) {
            EnumValue enumValue = (EnumValue) fieldValue;
            Set<EnumItem> items = enumValue.getValuesAsItems((EnumType) node.getField().getType());
            final List<String> values = Lists.newArrayList();
            for (final EnumItem item : items) {
                values.add(item.getLabel());
            }
            return Joiner.on(", ").join(values);

        } else if (fieldValue != null) {
            return fieldValue.toString();

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

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public String toString() {
        return "FieldColumn{" +
                "header='" + header + '\'' +
                ", criteria=" + criteria +
                '}';
    }
}
