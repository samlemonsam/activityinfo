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
package org.activityinfo.legacy.shared.reports.content;

import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.model.type.geo.Extents;

import java.io.Serializable;

public class AdminMarker implements Serializable {
    private int adminEntityId;
    private String name;
    private Double value;
    private String color;
    private Extents bounds;

    public AdminMarker() {

    }

    public AdminMarker(AdminEntityDTO entity) {
        this.adminEntityId = entity.getId();
        this.name = entity.getName();
        this.bounds = entity.getBounds();
    }

    public int getAdminEntityId() {
        return adminEntityId;
    }

    public void setAdminEntityId(int adminEntityId) {
        this.adminEntityId = adminEntityId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Extents getExtents() {
        return bounds;
    }

    public void setBounds(Extents bounds) {
        this.bounds = bounds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasValue() {
        return value != null;
    }

    @Override
    public String toString() {
        return name + "[" + adminEntityId + "] => " + value + " (" + color + ")";
    }
}