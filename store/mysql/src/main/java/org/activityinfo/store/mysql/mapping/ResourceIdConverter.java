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
package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceIdConverter {

    private char domain;
    private int columnIndex;

    public ResourceIdConverter(char domain, int columnIndex) {
        this.domain = domain;
        this.columnIndex = columnIndex;
    }

    public ResourceId toResourceId(ResultSet rs) {
        try {
            int id = rs.getInt(columnIndex);
            assert !rs.wasNull();
            return CuidAdapter.resourceId(domain, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
