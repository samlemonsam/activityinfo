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
package org.activityinfo.geoadmin.source;

import com.google.common.base.Function;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.geo.GeoArea;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class GeometryConverter implements Function<Object, FieldValue> {
    private final MathTransform transform;

    public GeometryConverter(GeometryType type) {
        try {
            transform = createTransform(type);
        } catch (FactoryException e) {
            throw new IllegalArgumentException("Could not create transform", e);
        }

    }

    private MathTransform createTransform(GeometryType type) throws FactoryException {
        CoordinateReferenceSystem sourceCrs = type.getCoordinateReferenceSystem();
        if(sourceCrs == null) {
            // if it's not WGS84, we'll soon find out as we check the geometry against the
            // country bounds
            sourceCrs = DefaultGeographicCRS.WGS84;
        }
        CoordinateReferenceSystem geoCRS = DefaultGeographicCRS.WGS84;
        boolean lenient = true; // allow for some error due to different datums
        return CRS.findMathTransform(sourceCrs, geoCRS, lenient);
    }

    public Geometry toWgs84(Object input) {
        assert input instanceof Geometry;

        Geometry geometry = (Geometry) input;
        try {
            return JTS.transform(geometry, transform);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FieldValue apply(Object input) {
        Envelope e = toWgs84(input).getEnvelopeInternal();
        return new GeoArea(new Extents(e.getMinY(), e.getMaxY(), e.getMinX(), e.getMaxX()));
    }
}
