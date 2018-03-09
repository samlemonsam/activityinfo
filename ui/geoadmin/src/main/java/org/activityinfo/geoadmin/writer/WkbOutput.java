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
package org.activityinfo.geoadmin.writer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBWriter;
import org.geotools.feature.FeatureCollection;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class WkbOutput implements OutputWriter {

    private DataOutputStream dataOut;
    private WKBWriter writer = new WKBWriter();
    private File outputFile;
    private ByteArrayOutputStream baos;
    private int numFeatures = 0;

    public WkbOutput(File outputDir, int adminLevelId) throws IOException {
        outputFile = new File(outputDir, adminLevelId + ".wkb.gz");
        baos = new ByteArrayOutputStream();
        dataOut = new DataOutputStream(baos);
    }

    @Override
    public void start(FeatureCollection features) throws IOException {

    }

    @Override
    public void write(int adminEntityId, Geometry geometry) throws IOException {
        if (!geometry.isValid()) {
            throw new IllegalStateException(adminEntityId + " has invalid geometry");
        }
        dataOut.writeInt(adminEntityId);
        writer.write(geometry, new OutputStreamOutStream(dataOut));
        numFeatures++;
    }

    @Override
    public void close() throws IOException {
        DataOutputStream out = new DataOutputStream(
            new GZIPOutputStream(
                new FileOutputStream(outputFile)));
        out.writeInt(numFeatures);
        out.write(baos.toByteArray());
        out.close();
    }

}
