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
package org.activityinfo.ui.client.util;

import org.activityinfo.model.type.geo.AiLatLng;
import org.activityinfo.model.type.geo.Extents;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.discotools.gwt.leaflet.client.types.LatLngBounds;

public final class LeafletUtil {

    private static final double EPSILON = 0.000001;
    
    private LeafletUtil() {}
    
    public static LatLngBounds newLatLngBounds(Extents bounds) {
        LatLng southWest = new LatLng(bounds.getMinLat(), bounds.getMinLon());
        LatLng northEast = new LatLng(bounds.getMaxLat(), bounds.getMaxLon());
        return new LatLngBounds(southWest, northEast);
    }

    public static LatLng to(AiLatLng latLng) {
        return new LatLng(latLng.getLat(), latLng.getLng());
    }

    public static String color(String color) {
        if (color == null) {
            return "#FF0000";
        } else if (color.startsWith("#")) {
            return color;
        } else {
            return "#" + color;
        }
    }

    public static boolean equal(LatLngBounds a, LatLngBounds b) {
        if (a != null && b != null) {
            LatLng northWest = a.getNorthWest();
            LatLng southEast = a.getSouthEast();
            if (northWest != null && southEast != null) {
                return equal(northWest, b.getNorthWest()) && equal(southEast, b.getSouthEast());
            }
        }
        return false;
    }

    public static boolean equal(LatLng a, LatLng b) {
        return a != null && b != null && 
                Math.abs(a.lat() - b.lat()) < EPSILON && 
                Math.abs(a.lng() - b.lng()) < EPSILON;
    }
}
