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
