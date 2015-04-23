package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoPoint;

class GeoPointFieldValueParser implements FieldValueParser {
    @Override
    public FieldValue parse(String text) {
        double latitude, longitude;

        if (text == null) throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");

        String coords[] = text.split("\\s+");
        if (coords.length < 2) {
            throw new IllegalArgumentException("Insufficient doubles in Element passed to OdkFieldValueParser.parse()");
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
