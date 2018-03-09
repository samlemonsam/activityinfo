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
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.metadata.CountryInstance;

import java.util.Collections;
import java.util.List;

public class CountryFieldBinding implements FieldBinding {

    private CountryInstance country;

    public CountryFieldBinding(CountryInstance country) {
        this.country = country;
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        for (int i=0; i<columnSet.getNumRows(); i++) {
            dataArray[i].set(LocationFieldBinding.LOCATION_ID_COLUMN, country.getLocationTypeId());
            dataArray[i].set(LocationFieldBinding.LOCATION_NAME_COLUMN, country.getCountryName());
            dataArray[i].set(LocationFieldBinding.LOCATION_CODE_COLUMN, country.getIso2());

            dataArray[i].set(GeoPointFieldBinding.GEO_LATITUDE_COLUMN, country.getBounds().getCenterY());
            dataArray[i].set(GeoPointFieldBinding.GEO_LONGITUDE_COLUMN, country.getBounds().getCenterX());
        }

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return Collections.emptyList();
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return Collections.emptyList();
    }
}
