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
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;

import static org.activityinfo.server.command.handler.binding.LocationFieldBinding.*;

public class GenericLocationFieldBinding implements FieldBinding {

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView id = columnSet.getColumnView(LOCATION_ID_COLUMN);
        ColumnView name = columnSet.getColumnView(LOCATION_NAME_COLUMN);
        ColumnView code = columnSet.getColumnView(LOCATION_CODE_COLUMN);

        for (int i=0; i<columnSet.getNumRows(); i++) {
            String idVal = id.getString(i);
            dataArray[i].set(LOCATION_ID_COLUMN, CuidAdapter.getLegacyIdFromCuid(idVal));

            dataArray[i].set(LOCATION_NAME_COLUMN, name.getString(i));
            dataArray[i].set(LOCATION_CODE_COLUMN, code.getString(i));
        }

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return getLocationQuery();
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return getLocationQuery();
    }

    public List<ColumnModel> getLocationQuery() {
        return Arrays.asList(
                new ColumnModel().setFormula(LOCATION_SYMBOL).as(LOCATION_ID_COLUMN),
                new ColumnModel().setFormula(new CompoundExpr(LOCATION_SYMBOL,NAME_SYMBOL)).as(LOCATION_NAME_COLUMN),
                new ColumnModel().setFormula(new CompoundExpr(LOCATION_SYMBOL,CODE_SYMBOL)).as(LOCATION_CODE_COLUMN)
        );
    }
}
