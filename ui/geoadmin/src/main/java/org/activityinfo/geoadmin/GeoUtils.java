package org.activityinfo.geoadmin;

import com.vividsolutions.jts.geom.Envelope;
import org.activityinfo.model.type.geo.Extents;

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

}
