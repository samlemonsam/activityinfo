package org.activityinfo.server.command.handler.binding;

import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.FunctionCallNode;
import org.activityinfo.model.expr.functions.BoundingBoxFunction;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.Extents;

import java.util.Arrays;
import java.util.List;

public class GeoAreaFieldBinding implements FieldBinding<AdminEntityDTO> {

    private final String XMIN = "xmin";
    private final String XMAX = "xmax";
    private final String YMIN = "ymin";
    private final String YMAX = "ymax";

    private final String bound = "boundary";

    private FormClass form;

    public GeoAreaFieldBinding(FormClass form) {
        this.form = form;
    }

    @Override
    public AdminEntityDTO[] extractFieldData(AdminEntityDTO[] dataArray, ColumnSet columnSet) {
        ColumnView xmin = columnSet.getColumnView(XMIN);
        ColumnView xmax = columnSet.getColumnView(XMAX);
        ColumnView ymin = columnSet.getColumnView(YMIN);
        ColumnView ymax = columnSet.getColumnView(YMAX);

        int levelId = CuidAdapter.getLegacyIdFromCuid(form.getId());

        for (int i=0; i<columnSet.getNumRows(); i++) {
            Extents bounds = Extents.create(xmin.getDouble(i), ymin.getDouble(i), xmax.getDouble(i), ymax.getDouble(i));
            dataArray[i].setBounds(bounds);
        }

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        ResourceId formId = form.getId();
        return Arrays.asList(
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.XMIN, new CompoundExpr(formId,bound))).as(XMIN),
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.XMAX, new CompoundExpr(formId,bound))).as(XMAX),
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.YMIN, new CompoundExpr(formId,bound))).as(YMIN),
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.YMAX, new CompoundExpr(formId,bound))).as(YMAX)
        );
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return null;
    }
}
