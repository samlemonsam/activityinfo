package org.activityinfo.ui.client.util;

import org.activityinfo.model.type.geo.AiLatLng;
import org.activityinfo.legacy.shared.reports.util.mapping.Extents;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.discotools.gwt.leaflet.client.types.LatLngBounds;

public class LeafletUtil {

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

    public static boolean equals(LatLngBounds b1, LatLngBounds b2) {
        if (b1 != null && b2 != null) {
            LatLng northWest = b1.getNorthWest();
            LatLng southEast = b1.getSouthEast();
            if (northWest != null && southEast != null) {
                return equals(northWest, b2.getNorthWest()) && equals(southEast, b2.getSouthEast());
            }
        }
        return false;
    }

    public static boolean equals(LatLng latLng1, LatLng latLng2) {
        if (latLng1 != null && latLng2 != null) {
            return latLng1.lat() == latLng2.lat() && latLng1.lng() == latLng2.lng();
        }
        return false;
    }
}
