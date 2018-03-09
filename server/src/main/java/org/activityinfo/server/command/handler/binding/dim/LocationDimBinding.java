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
package org.activityinfo.server.command.handler.binding.dim;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.metadata.Activity;

import java.util.Arrays;
import java.util.List;


public class LocationDimBinding extends DimBinding {

    private static final String ID_COLUMN = "LocationId";
    private static final String LABEL_COLUMN = "LocationName";
   
    private final Dimension model = new Dimension(DimensionType.Location);

    public LocationDimBinding() {
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        ResourceId locationFieldId = CuidAdapter.locationField(activityIdOf(formTree));
        return Arrays.asList(
                new ColumnModel().setFormula(locationFieldId).as(ID_COLUMN),
                new ColumnModel().setFormula(new CompoundExpr(locationFieldId, "label")).as(LABEL_COLUMN));
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(Activity activity, ColumnSet columnSet) {
        return extractEntityCategories(columnSet, ID_COLUMN, LABEL_COLUMN);
    }
}
