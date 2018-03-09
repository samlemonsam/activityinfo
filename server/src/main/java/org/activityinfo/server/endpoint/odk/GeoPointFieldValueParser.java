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
package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoPoint;

class GeoPointFieldValueParser implements FieldValueParser {
    @Override
    public FieldValue parse(String text) {
        double latitude, longitude;

        if (text == null) {
            throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");
        }

        String coords[] = text.split("\\s+");
        if (coords.length < 2) {
            throw new IllegalArgumentException("Malformed lat/lng from ODK: " + text);
        }

        try {
            latitude = Double.parseDouble(coords[0]);
            longitude = Double.parseDouble(coords[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unparsable double in Element passed to OdkFieldValueParser.parse()", e);
        }

        return new GeoPoint(latitude, longitude);
    }
}
