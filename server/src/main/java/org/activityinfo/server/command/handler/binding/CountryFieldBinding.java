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

            dataArray[i].set(GeoPointFieldBinding.GEO_LONGITUDE_COLUMN, country.getX1());
            dataArray[i].set(GeoPointFieldBinding.GEO_LATITUDE_COLUMN, country.getY1());
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
