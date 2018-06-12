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
package org.activityinfo.server.geo;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.InStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import freemarker.log.Logger;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Singleton
public class WkbGeometryProvider implements AdminGeometryProvider {

    private static final Logger LOGGER = Logger
            .getLogger(WkbGeometryProvider.class.getName());

    private GeometryFactory geometryFactory;

    public WkbGeometryProvider() {
        this.geometryFactory = new GeometryFactory();
    }

    @Override
    public List<AdminGeo> getGeometries(int adminLevelId) {
        try {
            List<AdminGeo> list = Lists.newArrayList();
            DataInputStream in = new DataInputStream(
                    openWkb(adminLevelId));
            WKBReader wkbReader = new WKBReader(geometryFactory);
            int count = in.readInt();
            for (int i = 0; i != count; ++i) {
                int id = in.readInt();
                LOGGER.info("Reading geometry for admin entity " + id);
                Geometry geometry = wkbReader.read(new DataInputInStream(in));
                list.add(new AdminGeo(id, geometry));
            }
            return list;
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream openWkb(int adminLevelId) {
        InputStream in = getClass().getResourceAsStream(
                "/" + adminLevelId + ".wkb");
        if (in == null) {
            throw new RuntimeException("No wkb geometry for level "
                    + adminLevelId + " on classpath");
        }
        return in;
    }

    private static class DataInputInStream implements InStream {
        private DataInput in;

        public DataInputInStream(DataInput in) {
            super();
            this.in = in;
        }

        @Override
        public void read(byte[] buf) throws IOException {
            in.readFully(buf);
        }
    }
}
