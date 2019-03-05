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
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Alternate DTO for UserDatabase for compatibility with the 2.x JSON API
 */
@JsonAutoDetect(JsonMethod.NONE)
public class UserDatabaseDTOWithForms extends BaseModelData implements EntityDTO {

    private CountryDTO country;
    private List<PartnerDTO> partners = new ArrayList<PartnerDTO>(0);
    private List<ActivityFormDTO> activities = new ArrayList<>(0);
    private Set<LockedPeriodDTO> lockedPeriods = new HashSet<LockedPeriodDTO>(0);
    private List<ProjectDTO> projects = new ArrayList<>(0);

    public final static String ENTITY_NAME = "UserDatabase";

    public UserDatabaseDTOWithForms() {
    }

    public UserDatabaseDTOWithForms(UserDatabaseDTO userDatabaseDTO) {
        setId(userDatabaseDTO.getId());
        setName(userDatabaseDTO.getName());
        setAmOwner(userDatabaseDTO.getAmOwner());
        setCountry(userDatabaseDTO.getCountry());
        setFullName(userDatabaseDTO.getFullName());
        setLockedPeriods(userDatabaseDTO.getLockedPeriods());
        setMyPartnerId(userDatabaseDTO.getMyPartnerId());
        setOwnerEmail(userDatabaseDTO.getOwnerEmail());
        setOwnerName(userDatabaseDTO.getOwnerName());
        setPartners(userDatabaseDTO.getPartners());
        setProjects(userDatabaseDTO.getProjects());
        setAllowNestedValues(userDatabaseDTO.isAllowNestedValues());
    }

    /**
     * @return this UserDatabase's id
     */
    @Override @JsonProperty
    @JsonView(DTOViews.List.class)
    public int getId() {
        return (Integer) get("id");
    }

    /**
     * Sets this UserDatabase's id
     */
    public void setId(int id) {
        set("id", id);
    }

    /**
     * @return the name of this UserDatabase
     */
    @Override
    @JsonProperty
    @JsonView({DTOViews.Schema.class, DTOViews.List.class})
    public String getName() {
        return get("name");
    }

    /**
     * Sets the name of this UserDatabase
     */
    public void setName(String name) {
        set("name", name);
    }

    /**
     * Sets the name of this UserDatabase's owner
     *
     * @param ownerName
     */
    public void setOwnerName(String ownerName) {
        set("ownerName", ownerName);
    }

    /**
     * @return the name of this UserDatabase's owner
     */
    public String getOwnerName() {
        return get("ownerName");
    }

    /**
     * Sets the email of this UserDatabase's owner
     */
    public void setOwnerEmail(String ownerEmail) {
        set("ownerEmail", ownerEmail);
    }

    /**
     * @return the email of this UserDatabase's owner
     */
    public String getOwnerEmail() {
        return get("ownerEmail");
    }

    /**
     * Sets the full, descriptive name of this UserDatabase
     */
    public void setFullName(String fullName) {
        set("fullName", fullName);
    }

    /**
     * Gets the full, descriptive name of this UserDatabase
     */
    @JsonProperty("description")
    @JsonView(DTOViews.Schema.class)
    public String getFullName() {
        return get("fullName");
    }

    /**
     * @return this list of ActivityDTOs that belong to this UserDatabase
     */
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public List<ActivityFormDTO> getActivities() {
        return activities;
    }

    /**
     * @param activities sets the list of Activities in this UserDatabase
     */
    public void setActivities(List<ActivityFormDTO> activities) {
        this.activities = activities;
    }

    /**
     * @return the Country in which this UserDatabase is set
     */
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public CountryDTO getCountry() {
        return country;
    }

    /**
     * Sets the Country to which this UserDatabase belongs
     */
    public void setCountry(CountryDTO country) {
        this.country = country;
    }

    /**
     * @return the list of Partners who belong to this UserDatabase
     */
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public List<PartnerDTO> getPartners() {
        return partners;
    }

    /**
     * Sets the list of Partners who belong to this UserDatabase
     */
    public void setPartners(List<PartnerDTO> partners) {
        this.partners = partners;
    }

    @JsonProperty @JsonView(DTOViews.Schema.class)
    public List<ProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }

    /**
     * @return the id of the Partner to which the client belongs
     */
    public int getMyPartnerId() {
        return (Integer) get("myPartnerId");
    }

    /**
     * Sets the id of the Partner to which the current user belongs
     */
    public void setMyPartnerId(int partnerId) {
        set("myPartnerId", partnerId);
    }

    /**
     * @return true if the client owns this UserDatabase
     */
    @JsonProperty("owned") @JsonView(DTOViews.Schema.class)
    public boolean getAmOwner() {
        return get("amOwner", false);
    }

    /**
     * Sets the flag to determine whether the current user is the owner of this
     * database.
     */
    public void setAmOwner(boolean value) {
        set("amOwner", value);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    public ActivityFormDTO getActivityById(int id) {
        for (ActivityFormDTO activity : getActivities()) {
            if (activity.getId() == id) {
                return activity;
            }
        }
        return null;
    }


    public void setLockedPeriods(Set<LockedPeriodDTO> lockedPeriods) {
        this.lockedPeriods = lockedPeriods;
    }
    
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public Set<LockedPeriodDTO> getLockedPeriods() {
        return lockedPeriods;
    }

}
