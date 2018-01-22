package org.activityinfo.server.command.handler.binding;

import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collections;
import java.util.List;

public class ConstantActivityIdFieldBinding extends ActivityIdFieldBinding {

    private ResourceId id;

    public ConstantActivityIdFieldBinding(int id) {
        this.id = CuidAdapter.activityFormClass(id);
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return Collections.singletonList(new ColumnModel().setExpression(new ConstantExpr(id.toString())).as(CLASS_ID_COLUMN));
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return Collections.singletonList(new ColumnModel().setExpression(new ConstantExpr(id.toString())).as(CLASS_ID_COLUMN));
    }
}
