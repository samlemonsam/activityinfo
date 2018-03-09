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
package org.activityinfo.server.report.renderer.image;

import com.google.code.appengine.awt.geom.GeneralPath;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import org.activityinfo.legacy.shared.reports.content.Point;
import org.activityinfo.model.type.geo.AiLatLng;
import org.activityinfo.server.report.generator.map.TiledMap;

public class PathUtils {

    public static GeneralPath toPath(TiledMap map, Geometry geometry) {
        GeneralPath path = new GeneralPath();
        for (int i = 0; i != geometry.getNumGeometries(); ++i) {
            Polygon polygon = (Polygon) geometry.getGeometryN(i);
            PathUtils.addRingToPath(map, path, polygon.getExteriorRing().getCoordinates());
            for (int j = 0; j != polygon.getNumInteriorRing(); ++j) {
                PathUtils.addRingToPath(map, path, polygon.getInteriorRingN(j).getCoordinates());
            }
            break;
        }
        return path;
    }

    private static void addRingToPath(TiledMap map, GeneralPath path, Coordinate[] coordinates) {
        System.out.println("--ring--");

        float lastX = Float.NaN;
        float lastY = Float.NaN;
        for (int j = 0; j != coordinates.length; ++j) {
            Point point = map.fromLatLngToPixel(new AiLatLng(coordinates[j].y, coordinates[j].x));
            float x = point.getX();
            float y = point.getY();

            if (x != lastX || y != lastY) {
                System.out.println(point.getX() + "," + point.getY());
                if (j == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            lastX = x;
            lastY = y;
        }
        path.closePath();
    }

}
