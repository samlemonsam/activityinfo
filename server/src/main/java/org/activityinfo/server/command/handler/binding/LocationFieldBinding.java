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
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.ConstantNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;

import java.util.Iterator;
import java.util.List;

import static org.activityinfo.server.command.handler.GetSitesHandler.getRange;

public class LocationFieldBinding implements FieldBinding {

    public static final SymbolNode ID_SYMBOL = new SymbolNode(ColumnModel.RECORD_ID_SYMBOL);
    public static final SymbolNode LOCATION_SYMBOL = new SymbolNode("location");
    public static final SymbolNode NAME_SYMBOL = new SymbolNode("name");
    public static final SymbolNode CODE_SYMBOL = new SymbolNode("axe");
    public static final String PARENT_SYMBOL = "parent";

    public static final String LOCATION_ID_COLUMN = "locationId";
    public static final String LOCATION_NAME_COLUMN = "locationName";
    public static final String LOCATION_CODE_COLUMN = "locationAxe";

    private static final ConstantNode ZEROED_ID = new ConstantNode(0);

    private final FormField locationField;
    private List<ResourceId> bound = Lists.newArrayList();
    private List<FieldBinding> locationBindings = Lists.newArrayList();

    public LocationFieldBinding(FormField locationField) {
        this.locationField = locationField;
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        for (FieldBinding locationBinding : locationBindings) {
            locationBinding.extractFieldData(dataArray, columnSet);
        }
        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        locationBindings.clear();

        // Query for generic location info - exists regardless of location range or type
        locationBindings.add(new GenericLocationFieldBinding());

        Iterator<ResourceId> locationRange = getRange(locationField);
        while (locationRange.hasNext()) {
            ResourceId locationReference = locationRange.next();
            FormClass locationForm = formTree.getFormClass(locationReference);
            buildLocationBindings(locationForm, formTree);
        }

        return buildColumnQuery(formTree);
    }

    private List<ColumnModel> buildColumnQuery(FormTree formTree) {
        List<ColumnModel> columnQuery = Lists.newArrayList();
        for (FieldBinding locationBinding : locationBindings) {
            columnQuery.addAll(locationBinding.getColumnQuery(formTree));
        }
        return columnQuery;
    }

    private void buildLocationBindings(FormClass locationForm, FormTree formTree) {
        if (alreadyBound(locationForm.getId())) {
            return;
        }
        switch(locationForm.getId().getDomain()) {
            case CuidAdapter.LOCATION_TYPE_DOMAIN:
                buildGeoBindings(locationForm);
                buildAdminBindings(locationForm, formTree, CuidAdapter.ADMIN_FIELD);
                break;
            case CuidAdapter.ADMIN_LEVEL_DOMAIN:
                locationBindings.add(new AdminEntityBinding(locationForm));
                buildGeoBindings(locationForm);
                buildAdminBindings(locationForm, formTree, CuidAdapter.ADMIN_PARENT_FIELD);
                break;
            default:
                break;
        }
    }

    private boolean alreadyBound(ResourceId locationFormId) {
        if (!bound.contains(locationFormId)) {
            bound.add(locationFormId);
            return false;
        } else {
            return true;
        }
    }

    private void buildGeoBindings(FormClass locationForm) {
        Optional<FormField> potentialGeoField = locationForm.getFieldIfPresent(CuidAdapter.field(locationForm.getId(),CuidAdapter.GEOMETRY_FIELD));
        if (potentialGeoField.isPresent()) {
            FormField geoField = potentialGeoField.get();
            if (geoField.getType() instanceof GeoPointType) {
                locationBindings.add(new GeoPointFieldBinding(geoField.getId()));
            } else if (geoField.getType() instanceof GeoAreaType && (geoField.getId().getDomain() != CuidAdapter.ADMIN_LEVEL_DOMAIN)) {
                // Do not add a geoarea query for an admin level form - query engine cannot support currently
                locationBindings.add(new GeoAreaFieldBinding(locationForm.getId()));
            }
        }
    }

    private void buildAdminBindings(FormClass locationForm, FormTree formTree, int adminFieldIndex) {
        Optional<FormField> potentialAdminField = locationForm.getFieldIfPresent(CuidAdapter.field(locationForm.getId(), adminFieldIndex));
        if (potentialAdminField.isPresent()) {
            FormField adminField = potentialAdminField.get();
            Iterator<ResourceId> adminLevelRange = getRange(adminField);
            while (adminLevelRange.hasNext()) {
                ResourceId adminLevelId = adminLevelRange.next();
                buildLocationBindings(formTree.getFormClass(adminLevelId), formTree);
            }
        }
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        locationBindings.clear();

        // Query for generic location info - exists regardless of location range or type
        locationBindings.add(new GenericLocationFieldBinding());

        return buildColumnQuery(targetFormId);
    }

    private List<ColumnModel> buildColumnQuery(ResourceId targetFormId) {
        List<ColumnModel> columnQuery = Lists.newArrayList();
        for (FieldBinding locationBinding : locationBindings) {
            columnQuery.addAll(locationBinding.getTargetColumnQuery(targetFormId));
        }
        return columnQuery;
    }

}
