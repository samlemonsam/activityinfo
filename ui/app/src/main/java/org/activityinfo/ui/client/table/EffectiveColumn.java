package org.activityinfo.ui.client.table;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.type.FieldType;

public class EffectiveColumn {

    private static int nextId = 1;

    private String id;
    private String label;
    private FieldType type;
    private ColumnModel columnModel;

    public EffectiveColumn(FormTree.Node node) {
        this.label = node.getField().getLabel();
        this.id = "col" + (nextId++);
        this.columnModel = new ColumnModel().setExpression(node.getPath()).as(id);
    }

    public FieldType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public ColumnModel getQueryModel() {
        return columnModel;
    }
}
