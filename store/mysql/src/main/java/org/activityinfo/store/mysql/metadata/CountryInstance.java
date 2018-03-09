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

import org.activityinfo.model.type.geo.Extents;

import java.io.Serializable;

public class CountryInstance implements Serializable {

    int countryId;
    int locationTypeId;
    String countryName;
    String iso2;
    Extents bounds;

    public CountryInstance(int countryId, int locationTypeId, String countryName, String iso2, double x1, double x2, double y1, double y2) {
        this.countryId = countryId;
        this.locationTypeId = locationTypeId;
        this.countryName = countryName;
        this.iso2 = iso2;
        this.bounds = Extents.create(x1, y1, x2, y2);
    }

    public int getCountryId() {
        return countryId;
    }

    public int getLocationTypeId() {
        return locationTypeId;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getIso2() {
        return iso2;
    }

    public Extents getBounds() {
        return bounds;
    }

}
