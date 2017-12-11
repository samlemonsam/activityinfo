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

    private CompoundExpr XMIN_COLUMN;
    private CompoundExpr XMAX_COLUMN;
    private CompoundExpr YMIN_COLUMN;
    private CompoundExpr YMAX_COLUMN;

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
        ColumnView xmin = columnSet.getColumnView(XMIN_COLUMN.asExpression());
        ColumnView xmax = columnSet.getColumnView(XMAX_COLUMN.asExpression());
        ColumnView ymin = columnSet.getColumnView(YMIN_COLUMN.asExpression());
        ColumnView ymax = columnSet.getColumnView(YMAX_COLUMN.asExpression());

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

        CompoundExpr boundExpr = new CompoundExpr(formId,bound);
        XMIN_COLUMN = new CompoundExpr(formId, XMIN);
        XMAX_COLUMN = new CompoundExpr(formId, XMAX);
        YMIN_COLUMN = new CompoundExpr(formId, YMIN);
        YMAX_COLUMN = new CompoundExpr(formId, YMAX);

        return Arrays.asList(
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.XMIN, boundExpr)).as(XMIN_COLUMN.asExpression()),
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.XMAX, boundExpr)).as(XMAX_COLUMN.asExpression()),
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.YMIN, boundExpr)).as(YMIN_COLUMN.asExpression()),
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.YMAX, boundExpr)).as(YMAX_COLUMN.asExpression())
        );
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return null;
    }
}
