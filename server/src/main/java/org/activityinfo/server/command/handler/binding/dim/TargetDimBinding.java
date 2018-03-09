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
import org.activityinfo.legacy.shared.reports.content.TargetCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.store.mysql.metadata.Activity;

import java.util.Arrays;

/**
 * Maps target
 */
public class TargetDimBinding extends DimBinding {
    private final Dimension model = new Dimension(DimensionType.Target);

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(Activity activity, ColumnSet columnSet) {
        
        DimensionCategory categories[] = new DimensionCategory[columnSet.getNumRows()];
        Arrays.fill(categories, TargetCategory.REALIZED);
        
        return categories;
    }

    @Override
    public DimensionCategory extractTargetCategory(Activity activity, ColumnSet columnSet, int rowIndex) {
        return TargetCategory.TARGETED;
    }
}
