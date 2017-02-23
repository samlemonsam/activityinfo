package org.activityinfo.geoadmin.model;

import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.type.geo.Extents;

public class AdminEntity {
    private int id;
    private Integer parentId;
    private String name;
    private String code;
    private Extents bounds;
    private boolean deleted;
    private int levelId;
    private Geometry geometry;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Extents getBounds() {
        return bounds;
    }

    public void setBounds(Extents bounds) {
        this.bounds = bounds;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }
    
    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getLevelId() {
		return levelId;
	}

	public void setLevelId(int levelId) {
		this.levelId = levelId;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdminEntity other = (AdminEntity) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
    public String toString() {
        if (code == null) {
            return name;
        } else {
            return name + " (" + code + ")";
        }
    }
}
