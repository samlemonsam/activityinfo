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
package org.activityinfo.legacy.shared.model;

public enum PermissionType {

    VIEW                ("allowView"),
    CREATE              ("allowCreate"),
    EDIT                ("allowEdit"),
    DELETE              ("allowDelete"),
    MANAGE_USERS        ("allowManageUsers"),
    VIEW_ALL            ("allowViewAll"),
    CREATE_ALL          ("allowCreateAll"),
    EDIT_ALL            ("allowEditAll"),
    DELETE_ALL          ("allowDeleteAll"),
    MANAGE_ALL_USERS    ("allowManageAllUsers"),
    DESIGN              ("allowDesign"),
    EXPORT_RECORDS      ("allowExport");

    private final String dtoPropertyName;

    PermissionType(String dtoPropertyName) {
        this.dtoPropertyName = dtoPropertyName;
    }

    public String getDtoPropertyName() {
        return dtoPropertyName;
    }

}
