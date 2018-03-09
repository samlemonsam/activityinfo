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
package org.activityinfo.store.query.shared.join;

import com.google.common.collect.Iterables;
import org.activityinfo.model.formula.functions.StatFunction;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.store.query.shared.Aggregation;
import org.activityinfo.store.query.shared.Slot;

import java.util.List;


public class JoinedSubFormColumnViewSlot implements Slot<ColumnView> {

    private List<SubFormJoin> links;
    private Slot<ColumnView> nestedColumn;

    private StatFunction statistic;
    
    private ColumnView result;

    public JoinedSubFormColumnViewSlot(List<SubFormJoin> links, Slot<ColumnView> nestedColumn, StatFunction statistic) {
        this.links = links;
        this.nestedColumn = nestedColumn;
        this.statistic = statistic;
    }

    @Override
    public ColumnView get() {
        if(result == null) {
            result = join();
        }
        return result;
    }

    private ColumnView join() {

        // The nested column may contain multiple rows 
        // for each row on the left
        ColumnView subColumn = nestedColumn.get();
        int numSubRows = subColumn.numRows();

        // In order to produce a column with one summarized entry per 
        // output row, we need to assign "groupIds" going from
        // parentId -> master row index via the primary key

        int masterRowId[] = new int[numSubRows];
        double subColumnValues[] = new double[numSubRows];

        SubFormJoin join = Iterables.getOnlyElement(links);
        PrimaryKeyMap parentLookup = join.getMasterPrimaryKey().get();
        ColumnView parentColumn = join.getParentColumn().get();

        for (int i = 0; i < numSubRows; ++i) {
            // Get the parent id of this row
            String parentId = parentColumn.getString(i);
            masterRowId[i] = parentLookup.getRowIndex(parentId);

            // Store the value
            subColumnValues[i] = subColumn.getDouble(i);
        }
        
        int numMasterRows = parentLookup.numRows();

        // Sort the data values by master row index
        double[] result = Aggregation.sortAndAggregate(statistic, masterRowId, subColumnValues, numSubRows, numMasterRows);
        
        return new DoubleArrayColumnView(result);
    }

}
