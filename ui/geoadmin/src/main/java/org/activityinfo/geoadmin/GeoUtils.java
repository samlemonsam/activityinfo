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
