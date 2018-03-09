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

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;

public class StartEndDateFieldBinding implements FieldBinding {

    public static final String START_DATE_COLUMN = "date1";
    public static final String END_DATE_COLUMN = "date2";

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView startDate = columnSet.getColumnView(START_DATE_COLUMN);
        ColumnView endDate = columnSet.getColumnView(END_DATE_COLUMN);

        for (int i=0; i<columnSet.getNumRows(); i++) {
            if (!startDate.isMissing(i)) {
                dataArray[i].set(START_DATE_COLUMN, LocalDate.parse(startDate.getString(i)));
            }
            if (!endDate.isMissing(i)) {
                dataArray[i].set(END_DATE_COLUMN, LocalDate.parse(endDate.getString(i)));
            }
        }

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return getTargetColumnQuery(formTree.getRootFormId());
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return Arrays.asList(
                new ColumnModel().setFormula(START_DATE_COLUMN).as(START_DATE_COLUMN),
                new ColumnModel().setFormula(END_DATE_COLUMN).as(END_DATE_COLUMN)
        );
    }

}
