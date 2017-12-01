package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
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

    public static final SymbolExpr ID_SYMBOL = new SymbolExpr(ColumnModel.ID_SYMBOL);
    public static final SymbolExpr LOCATION_SYMBOL = new SymbolExpr("location");
    public static final SymbolExpr NAME_SYMBOL = new SymbolExpr("name");
    public static final SymbolExpr CODE_SYMBOL = new SymbolExpr("axe");
    public static final String PARENT_SYMBOL = "parent";

    public static final String LOCATION_ID_COLUMN = "locationId";
    public static final String LOCATION_NAME_COLUMN = "locationName";
    public static final String LOCATION_CODE_COLUMN = "locationAxe";

    private static final ConstantExpr ZEROED_ID = new ConstantExpr(0);

    private FormClass locationForm;
    private boolean isAdminLevelDomain;

    public LocationFieldBinding(FormClass locationForm) {
        this.locationForm = locationForm;
        Character domain = locationForm.getId().getDomain();
        this.isAdminLevelDomain = domain.equals(CuidAdapter.ADMIN_LEVEL_DOMAIN);
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView id = columnSet.getColumnView(LOCATION_ID_COLUMN);
        ColumnView name = columnSet.getColumnView(LOCATION_NAME_COLUMN);
        ColumnView code = columnSet.getColumnView(LOCATION_CODE_COLUMN);

        for (int i=0; i<columnSet.getNumRows(); i++) {
            if (isAdminLevelDomain) {
                Double idVal = id.getDouble(i);
                dataArray[i].set(LOCATION_ID_COLUMN, idVal.intValue());
            } else {
                String idVal = id.getString(i);
                dataArray[i].set(LOCATION_ID_COLUMN, CuidAdapter.getLegacyIdFromCuid(idVal));
            }
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
        ExprNode idExpr;
        idExpr = isAdminLevelDomain ? ZEROED_ID : LOCATION_SYMBOL;
        return Arrays.asList(
                new ColumnModel().setExpression(idExpr).as(LOCATION_ID_COLUMN),
                new ColumnModel().setExpression(new CompoundExpr(LOCATION_SYMBOL,NAME_SYMBOL)).as(LOCATION_NAME_COLUMN),
                new ColumnModel().setExpression(new CompoundExpr(LOCATION_SYMBOL,CODE_SYMBOL)).as(LOCATION_CODE_COLUMN)
        );
    }

}
