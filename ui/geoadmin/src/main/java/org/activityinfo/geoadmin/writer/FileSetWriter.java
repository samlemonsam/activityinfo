package org.activityinfo.geoadmin.writer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.geotools.feature.FeatureCollection;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;

public class FileSetWriter implements OutputWriter {

    private List<OutputWriter> writers = Lists.newArrayList();

    public FileSetWriter(int adminLevelId) throws IOException {
        File outputDir = new File("C:\\Users\\Alex\\aigeodb\\geometry");
        writers.add(new GoogleMapsWriter(outputDir, adminLevelId));
        writers.add(new WkbOutput(outputDir, adminLevelId));
    }

    @Override
    public void start(FeatureCollection features) throws IOException {
        for (OutputWriter writer : writers) {
            writer.start(features);
        }
    }

    @Override
    public void write(int adminEntityId, Geometry geometry) throws IOException {
        for (OutputWriter writer : writers) {
            writer.write(adminEntityId, geometry);
        }
    }

    @Override
    public void close() throws IOException {
        for (OutputWriter writer : writers) {
            writer.close();
        }
    }

}
