package org.activityinfo.geoadmin.model;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonView;

import com.google.common.collect.Lists;

public class Location {

	private int id;
	private String name;
	private Double latitude;
	private Double longitude;
	private Map<Integer, AdminEntity> adminEntities;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Map<Integer, AdminEntity> getAdminEntities() {
		return adminEntities;
	}
	public void setAdminEntities(Map<Integer, AdminEntity> adminEntities) {
		this.adminEntities = adminEntities;
	}
	
}
