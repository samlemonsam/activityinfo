package org.activityinfo.geoadmin;

import org.activityinfo.model.type.geo.Extents;

import com.vividsolutions.jts.geom.Envelope;

public class GeoUtils {

    public static Envelope toEnvelope(Extents bounds) {
        return new Envelope(bounds.getX1(), bounds.getX2(), bounds.getY1(), bounds.getY2());
    }

    public static Extents toBounds(Envelope envelope) {
        Extents bounds = Extents.empty();
        bounds.setX1(envelope.getMinX());
        bounds.setY1(envelope.getMinY());
        bounds.setX2(envelope.getMaxX());
        bounds.setY2(envelope.getMaxY());
        return bounds;
    }

    public static Extents toExtents(Envelope envelope) {
        return new Extents(
                envelope.getMinY(), envelope.getMaxY(),
                envelope.getMinX(), envelope.getMaxX());
    }
}
