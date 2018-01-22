package org.activityinfo.server.command.handler.binding;

import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.FunctionCallNode;
import org.activityinfo.model.expr.functions.BoundingBoxFunction;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.Extents;

import java.util.Arrays;
import java.util.List;

public class GeoAreaFieldBinding implements FieldBinding<AdminEntityDTO> {

    private static final String XMIN = "xmin";
    private static final String XMAX = "xmax";
    private static final String YMIN = "ymin";
    private static final String YMAX = "ymax";

    private static final String bound = "boundary";

    private final CompoundExpr boundExpr;
    private final CompoundExpr XMIN_COLUMN;
    private final CompoundExpr XMAX_COLUMN;
    private final CompoundExpr YMIN_COLUMN;
    private final CompoundExpr YMAX_COLUMN;

    public GeoAreaFieldBinding(ResourceId locationFormId) {
        this.boundExpr = new CompoundExpr(locationFormId,bound);
        this.XMIN_COLUMN = new CompoundExpr(locationFormId, XMIN);
        this.XMAX_COLUMN = new CompoundExpr(locationFormId, XMAX);
        this.YMIN_COLUMN = new CompoundExpr(locationFormId, YMIN);
        this.YMAX_COLUMN = new CompoundExpr(locationFormId, YMAX);
    }

    @Override
    public AdminEntityDTO[] extractFieldData(AdminEntityDTO[] dataArray, ColumnSet columnSet) {
        ColumnView xmin = columnSet.getColumnView(XMIN_COLUMN.asExpression());
        ColumnView xmax = columnSet.getColumnView(XMAX_COLUMN.asExpression());
        ColumnView ymin = columnSet.getColumnView(YMIN_COLUMN.asExpression());
        ColumnView ymax = columnSet.getColumnView(YMAX_COLUMN.asExpression());

        for (int i=0; i<columnSet.getNumRows(); i++) {
            if (allDefinedForRow(xmin, xmax, ymin, ymax, i)) {
                Extents bounds = Extents.create(xmin.getDouble(i), ymin.getDouble(i), xmax.getDouble(i), ymax.getDouble(i));
                dataArray[i].setBounds(bounds);
            }
        }

        return dataArray;
    }

    private boolean allDefinedForRow(ColumnView xmin, ColumnView xmax, ColumnView ymin, ColumnView ymax, int row) {
        return !xmin.isMissing(row)
                && !xmax.isMissing(row)
                && !ymin.isMissing(row)
                && !ymax.isMissing(row);
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return getGeoAreaQuery();
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return getGeoAreaQuery();
    }

    private List<ColumnModel> getGeoAreaQuery() {
        return Arrays.asList(
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.XMIN, boundExpr)).as(XMIN_COLUMN.asExpression()),
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.XMAX, boundExpr)).as(XMAX_COLUMN.asExpression()),
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.YMIN, boundExpr)).as(YMIN_COLUMN.asExpression()),
                new ColumnModel().setExpression(new FunctionCallNode(BoundingBoxFunction.YMAX, boundExpr)).as(YMAX_COLUMN.asExpression())
        );
    }
}
