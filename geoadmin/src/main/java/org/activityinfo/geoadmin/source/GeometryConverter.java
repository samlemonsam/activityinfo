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

    @Override
    public FieldValue apply(Object input) {
        assert input instanceof Geometry;

        Geometry geometryInWgs84;
        Geometry geometry = (Geometry) input;
        try {
            geometryInWgs84 = JTS.transform(geometry, transform);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }
        
        Envelope envelope = geometry.getEnvelopeInternal();
        Extents extents = Extents.create(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
        GeoArea fieldValue = new GeoArea(extents, "");
        return fieldValue;
    }
}
