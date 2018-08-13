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
package org.activityinfo.store.mysql.metadata;

import java.io.Serializable;

public class UserPermission implements Serializable {
    boolean view;
    boolean viewAll;
    boolean edit;
    boolean editAll;
    int partnerId;
    boolean design;
    String model;

    public boolean isView() {
        return view;
    }

    public boolean isViewAll() {
        return viewAll;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public boolean isEdit() {
        return edit;
    }

    public boolean isEditAll() {
        return editAll;
    }

    public String getModel() {
        return model;
    }

    public static UserPermission viewAll() {
        UserPermission permission = new UserPermission();
        permission.viewAll = true;
        permission.view = true;
        return permission;
    }

    public boolean isDesign() {
        return design;
    }
}
