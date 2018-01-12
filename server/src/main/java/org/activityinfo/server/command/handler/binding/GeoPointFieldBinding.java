package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;

public class GeoPointFieldBinding implements FieldBinding {

    public static final String LAT_SYMBOL = "latitude";
    public static final String LONG_SYMBOL = "longitude";

    public static final String GEO_LATITUDE_COLUMN = "y";
    public static final String GEO_LONGITUDE_COLUMN = "x";

    private CompoundExpr latExpr;
    private CompoundExpr lonExpr;

    public GeoPointFieldBinding(ResourceId geoFieldId) {
        this.latExpr = new CompoundExpr(geoFieldId, LAT_SYMBOL);
        this.lonExpr = new CompoundExpr(geoFieldId, LONG_SYMBOL);
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView latitude = columnSet.getColumnView(latExpr.asExpression());
        ColumnView longitude = columnSet.getColumnView(lonExpr.asExpression());

        for (int i=0; i<columnSet.getNumRows(); i++) {
            if(!latitude.isMissing(i)) {
                dataArray[i].set(GEO_LATITUDE_COLUMN, latitude.getDouble(i));
            }
            if(!longitude.isMissing(i)) {
                dataArray[i].set(GEO_LONGITUDE_COLUMN, longitude.getDouble(i));
            }
        }

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return getGeoPointQuery();
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return getGeoPointQuery();
    }

    private List<ColumnModel> getGeoPointQuery() {
        return Arrays.asList(
                new ColumnModel().setExpression(latExpr).as(latExpr.asExpression()),
                new ColumnModel().setExpression(lonExpr).as(lonExpr.asExpression())
        );
    }
}
