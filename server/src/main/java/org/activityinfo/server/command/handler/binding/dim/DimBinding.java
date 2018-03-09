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

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.command.handler.binding.FieldBinding;
import org.activityinfo.store.mysql.metadata.Activity;

import java.util.Collections;
import java.util.List;

/**
 * Retrieves values for the dimension
 */
public abstract class DimBinding implements FieldBinding<BaseModelData> {

    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        return dataArray;
    }

    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return Collections.emptyList();
    }
    
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) { return Collections.emptyList(); }

    public abstract Dimension getModel();
    
    public abstract DimensionCategory[] extractCategories(Activity activity, ColumnSet columnSet);
    
    public DimensionCategory extractTargetCategory(Activity activity, ColumnSet columnSet, int rowIndex) {
        return null;
    }
    
    protected final int activityIdOf(FormTree formTree) {
        return CuidAdapter.getLegacyIdFromCuid(formTree.getRootFormId());
    }

    protected final DimensionCategory[] extractEntityCategories(ColumnSet columnSet, String idColumn, String labelColumn) {
        ColumnView id = columnSet.getColumnView(idColumn);
        ColumnView label = columnSet.getColumnView(labelColumn);

        int numRows = columnSet.getNumRows();

        DimensionCategory categories[] = new DimensionCategory[numRows];

        for (int i = 0; i < numRows; i++) {
            categories[i] = extractEntityCategory(id, label, i);
        }

        return categories;
    }

    protected EntityCategory extractEntityCategory(ColumnView id, ColumnView label, int i) {
        String idString = id.getString(i);
        String labelString = label.getString(i);

        if(idString != null && labelString != null) {
            return new EntityCategory(CuidAdapter.getLegacyIdFromCuid(idString), label.getString(i));
        } else {
            return null;
        }
    }
}
