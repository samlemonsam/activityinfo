package org.activityinfo.ui.client.input.view;

import com.google.gwt.cell.client.Cell;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;

public class FieldContext extends Cell.Context {

    private Cell<?> cell;

    public FieldContext(ResourceId id, Cell<?> cell) {
        super(0, 0, id.asString());
        this.cell = cell;
    }

    public FieldContext(FormTree.Node node, Cell<?> cell) {
        this(node.getFieldId(), cell);
    }

    public Cell<?> getCell() {
        return cell;
    }
}
