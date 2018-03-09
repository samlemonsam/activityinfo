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
package org.activityinfo.legacy.shared.model;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.Extents;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.ArrayList;
import java.util.List;

/**
 * LocationType Data Transfer Object (DTO)
 *
 * @author Alex Bertram
 */
@JsonAutoDetect(JsonMethod.NONE)
public final class LocationTypeDTO extends BaseModelData implements EntityDTO, IsFormClass {

    public static final int GLOBAL_COUNTRY_ID = 3;
    public static final int GLOBAL_NULL_LOCATION_TYPE = 51545;

    public static final String NATIONWIDE_NAME = "Country";

    public static final int NAME_MAX_LENGTH = 50;

    public static final String OPEN_WORKFLOW_ID = "open";
    public static final String CLOSED_WORKFLOW_ID = "closed";

    private Integer databaseId;
    private List<AdminLevelDTO> adminLevels = new ArrayList<>();
    private Extents countryBounds;
    private long version;
    private long childVersion;
    private boolean deleted;

    public LocationTypeDTO() {
    }

    public LocationTypeDTO(int id, String name) {
        setId(id);
        setName(name);
    }

    @Override
    public ResourceId getResourceId() {
        return CuidAdapter.locationFormClass(getId());
    }

    public void setId(int id) {
        set("id", id);
    }

    @Override
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public int getId() {
        return (Integer) get("id");
    }

    @Override
    public String getEntityName() {
        return "LocationType";
    }

    public void setName(String value) {
        set("name", value);
    }

    @Override
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public String getName() {
        return get("name");
    }

    /**
     * 
     * @return true if this location type represents the nationwide or "nullary" location type 
     * used by activities that actually have no geography.
     */
    public boolean isNationwide() { 
        // Very special case for the Global null location type
        // which is not named "Country" but "Global"
        if(getId() == GLOBAL_NULL_LOCATION_TYPE) {
            return true;
        }
        return NATIONWIDE_NAME.equals(getName()) &&
               getId() != GLOBAL_NULL_LOCATION_TYPE &&
                getBoundAdminLevelId() == null && 
                getDatabaseId() == null;
    }

    @JsonProperty("adminLevelId") @JsonView(DTOViews.Schema.class)
    public Integer getBoundAdminLevelId() {
        return get("boundAdminLevelId");
    }

    public void setBoundAdminLevelId(Integer id) {
        set("boundAdminLevelId", id);
    }

    public boolean isAdminLevel() {
        return getBoundAdminLevelId() != null;
    }

    public void setWorkflowId(String workflowId) {
        set("workflowId", workflowId);
    }

    public String getWorkflowId() {
        return get("workflowId");
    }

    public Integer getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Integer databaseId) {
        this.databaseId = databaseId;
    }

    public List<AdminLevelDTO> getAdminLevels() {
        if (adminLevels == null) {
            adminLevels = new ArrayList<>();
        }
        return adminLevels;
    }

    public void setAdminLevels(List<AdminLevelDTO> adminLevels) {
        this.adminLevels = adminLevels;
    }

    public Extents getCountryBounds() {
        return countryBounds;
    }

    public void setCountryBounds(Extents countryBounds) {
        this.countryBounds = countryBounds;
    }

    public long getChildVersion() {
        return childVersion;
    }

    public void setChildVersion(long childVersion) {
        this.childVersion = childVersion;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
