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
package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.common.base.Strings;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;


class LocationTypeEntry extends BaseModelData implements Comparable<LocationTypeEntry> {

    private boolean builtin;
    private String databaseName;
    private String locationTypeName;

    private LocationTypeEntry() {
    }


    /**
     * Create an entry for a LocationType from the global admin reference database
     */
    public LocationTypeEntry(LocationTypeDTO locationType) {
        set("id", locationType.getId());
        set("label", locationType.getName());
        this.locationTypeName = locationType.getName();
        this.databaseName = "";
        this.builtin = true;
    }

    public int getId() {
        return get("id");
    }

    public boolean isPublic() {
        return !builtin && databaseName.isEmpty();
    }

    /**
     * Create an entry for a locationType
     * @param locationTypeDTO
     * @param databaseName
     */
    public LocationTypeEntry(LocationTypeDTO locationTypeDTO, String databaseName) {
        set("id", locationTypeDTO.getId());
        set("label", locationTypeDTO.getName());
        this.builtin = false;
        this.databaseName = Strings.nullToEmpty(databaseName);
        this.locationTypeName = locationTypeDTO.getName();
    }

    public String getHeader() {
        if(builtin) {
            return I18N.CONSTANTS.builtInLocationTypes();
        } else if(databaseName.isEmpty()) {
            return I18N.CONSTANTS.publicLocationTypes();
        } else {
            return databaseName;
        }
    }

    public String getLocationTypeName() {
        return locationTypeName;
    }

    @Override
    public int compareTo(LocationTypeEntry other) {
        // "Reference" location types from our GeoDB come first...
        if(this.builtin != other.builtin) {
            return this.builtin ? -1 : +1;
        }

        // Then sort by database name...
        if(!this.databaseName.equals(other.databaseName)) {
            if(this.databaseName.isEmpty()) {
                return +1;
            } else if(other.databaseName.isEmpty()) {
                return -1;
            } else {
                return this.databaseName.compareTo(databaseName);
            }
        }

        // Otherwise sort on the location type name.
        return this.locationTypeName.compareToIgnoreCase(other.locationTypeName);
    }

}
