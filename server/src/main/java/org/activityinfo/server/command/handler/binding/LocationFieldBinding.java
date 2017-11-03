package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;

public class LocationFieldBinding implements FieldBinding {

    public static final String ID_SYMBOL = ColumnModel.ID_SYMBOL;
    public static final String NAME_SYMBOL = "name";
    public static final String CODE_SYMBOL = "code";
    public static final String PARENT_SYMBOL = "parent";

    public static final String LOCATION_ID_COLUMN = "locationId";
    public static final String LOCATION_NAME_COLUMN = "locationName";
    public static final String LOCATION_CODE_COLUMN = "locationAxe";

    private FormClass locationForm;

    public LocationFieldBinding(FormClass locationForm) {
        this.locationForm = locationForm;
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView id = columnSet.getColumnView(LOCATION_ID_COLUMN);
        ColumnView name = columnSet.getColumnView(LOCATION_NAME_COLUMN);
        ColumnView code = columnSet.getColumnView(LOCATION_CODE_COLUMN);

        for (int i=0; i<columnSet.getNumRows(); i++) {
            dataArray[i].set(LOCATION_ID_COLUMN, CuidAdapter.getLegacyIdFromCuid(id.getString(i)));
            dataArray[i].set(LOCATION_NAME_COLUMN, name.getString(i));
            dataArray[i].set(LOCATION_CODE_COLUMN, code.getString(i));
        }

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return getTargetColumnQuery(locationForm.getId());
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return Arrays.asList(
                new ColumnModel().setExpression(new CompoundExpr(targetFormId,ID_SYMBOL)).as(LOCATION_ID_COLUMN),
                new ColumnModel().setExpression(new CompoundExpr(targetFormId,NAME_SYMBOL)).as(LOCATION_NAME_COLUMN),
                new ColumnModel().setExpression(new CompoundExpr(targetFormId,CODE_SYMBOL)).as(LOCATION_CODE_COLUMN)
        );
    }

}
