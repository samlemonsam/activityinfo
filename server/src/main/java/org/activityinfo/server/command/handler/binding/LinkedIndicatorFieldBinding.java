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
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnView;

public class LinkedIndicatorFieldBinding extends IndicatorFieldBinding {

    private final int destinationId;

    public LinkedIndicatorFieldBinding(int destinationId, FormField indicatorField) {
        super(indicatorField);
        this.destinationId = destinationId;
    }

    public LinkedIndicatorFieldBinding(FormField indicatorField) {
        super(indicatorField);
        this.destinationId = CuidAdapter.getLegacyIdFromCuid(indicatorField.getId());
    }

    @Override
    protected void getQuantityIndicator(BaseModelData[] dataArray, ColumnView indicatorColumn) {
        for (int i=0; i<indicatorColumn.numRows(); i++) {
            Double value = indicatorColumn.getDouble(i);
            if (value != null && !value.isNaN()) {
                dataArray[i].set(IndicatorDTO.getPropertyName(destinationId), value);
            }
        }
    }

    @Override
    protected void getTextIndicator(BaseModelData[] dataArray, ColumnView indicatorColumn) {
        for (int i=0; i<indicatorColumn.numRows(); i++) {
            String value = indicatorColumn.getString(i);
            if (value != null && !value.isEmpty()) {
                dataArray[i].set(IndicatorDTO.getPropertyName(destinationId), value);
            }
        }
    }
}
