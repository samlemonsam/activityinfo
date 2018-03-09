/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.CompoundExpr;
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
                new ColumnModel().setFormula(latExpr).as(latExpr.asExpression()),
                new ColumnModel().setFormula(lonExpr).as(lonExpr.asExpression())
        );
    }
}
