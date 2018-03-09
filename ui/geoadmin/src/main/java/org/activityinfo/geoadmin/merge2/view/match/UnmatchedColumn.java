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
package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Optional;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.query.ColumnView;


/**
 * Displays the contents of a Field in the source or target collection that is not matched. 
 */
public class UnmatchedColumn extends MatchTableColumn {

    private final FieldProfile field;
    private final MatchSide side;
    private ColumnView columnView;

    public UnmatchedColumn(FieldProfile field, MatchSide side, ColumnView columnView) {
        this.field = field;
        this.side = side;
        this.columnView = columnView;
    }

    @Override
    public String getHeader() {
        return field.getLabel();
    }

    @Override
    public String getValue(int rowIndex) {
        if(field.getView() != null) {
            Object value = columnView.get(rowIndex);
            if(value != null) {
                return value.toString();
            }
        }
        return null;
    }

    @Override
    public Optional<MatchSide> getSide() {
        return Optional.of(side);
    }

}
