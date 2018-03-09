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
package org.activityinfo.geoadmin.locations;

import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Point;
import org.activityinfo.geoadmin.ImportFeature;
import org.activityinfo.geoadmin.model.AdminEntity;

import com.google.common.collect.Maps;

public class LocationFeature {
	private ImportFeature feature;
	private Map<Integer, AdminEntity> entities;
    private int id;

    public LocationFeature(ImportFeature feature) {
		this.feature = feature;
		this.entities = Maps.newHashMap();
	}

	public ImportFeature getFeature() {
		return feature;
	}

	public Map<Integer, AdminEntity> getEntities() {
		return entities;
	}

    public Point getPoint() {
        return toPoint(feature.getGeometry());
    }

    private Point toPoint(Geometry geometry) {
        if(geometry instanceof Point) {
            return (Point) geometry;
        } else if(geometry instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection) geometry;
            if(gc.getNumGeometries() == 1) {
                return toPoint(gc.getGeometryN(0));
            }
        }
        throw new IllegalArgumentException("Expected point geometry");
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
