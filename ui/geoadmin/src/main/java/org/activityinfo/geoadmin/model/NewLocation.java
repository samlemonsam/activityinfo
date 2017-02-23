package org.activityinfo.geoadmin.model;

import java.util.List;

import com.google.common.collect.Lists;

public class NewLocation {
	private String name;
	private List<Integer> adminEntityIds = Lists.newArrayList();
	private Double latitude;
	private Double longitude;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Integer> getAdminEntityIds() {
		return adminEntityIds;
	}
	public void setAdminEntityIds(List<Integer> adminEntityIds) {
		this.adminEntityIds = adminEntityIds;
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
	
}
