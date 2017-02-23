package org.activityinfo.geoadmin.model;

import com.vividsolutions.jts.geom.Point;


public class LatLng {
	private double lat;
	private double lng;
	
	public LatLng() {
		
	}
	
	public LatLng(Point point) {
		this.lat = point.getY();
		this.lng = point.getX();
	}
	
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	
	
}
