package org.activityinfo.geoadmin.writer;

import java.io.IOException;

import org.geotools.feature.FeatureCollection;

import com.vividsolutions.jts.geom.Geometry;

public interface OutputWriter {

    void start(FeatureCollection features) throws IOException;

    void write(int adminEntityId, Geometry geometry) throws IOException;

    void close() throws IOException;
}
