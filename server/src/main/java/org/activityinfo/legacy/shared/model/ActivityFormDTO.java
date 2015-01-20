package org.activityinfo.legacy.shared.model;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.legacy.shared.model.LockedPeriodDTO.HasLockedPeriod;
import org.activityinfo.legacy.shared.reports.util.mapping.Extents;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.*;

/**
 * One-to-one DTO for the Activity table.
 *
 * @author Alex Bertram
 */
@JsonAutoDetect(JsonMethod.NONE)
public final class ActivityFormDTO extends BaseModelData implements EntityDTO, HasLockedPeriod, ProvidesKey, IsFormClass, IsActivityDTO {

    public static final String ENTITY_NAME = "Activity";

    public static final int REPORT_ONCE = 0;
    public static final int REPORT_MONTHLY = 1;

    public static final int NAME_MAX_LENGTH = 255;
    public static final int CATEGORY_MAX_LENGTH = 255;

    private int ownerUserId;
    private int databaseId;
    private String databaseName;

    private List<IndicatorDTO> indicators = new ArrayList<IndicatorDTO>(0);
    private List<AttributeGroupDTO> attributeGroups = new ArrayList<AttributeGroupDTO>(0);
    private Set<LockedPeriodDTO> lockedPeriods = new HashSet<LockedPeriodDTO>(0);

    // to ensure serializer
    private Published _published;
    private List<PartnerDTO> partners;
    private List<ProjectDTO> projects;
    private LocationTypeDTO locationType;

    private boolean editAllowed;
    private boolean editAllAllowed;
    private boolean designAllowed;
    private int currentPartnerId;

    public ActivityFormDTO() {
        setReportingFrequency(REPORT_ONCE);
    }

    /**
     * Constructs a DTO with the given properties
     */
    public ActivityFormDTO(Map<String, Object> properties) {
        super(properties);
    }

    @Override
    public ResourceId getResourceId() {
        return CuidAdapter.activityFormClass(getId());
    }

    /**
     * Creates a shallow clone
     *
     * @param model
     */
    public ActivityFormDTO(ActivityFormDTO model) {
        super(model.getProperties());
        this.databaseId = model.getDatabaseId();
        this.setLocationType(model.getLocationType());
        this.setIndicators(model.getIndicators());
        this.setAttributeGroups(model.getAttributeGroups());
    }

    public ActivityFormDTO(ActivityDTO source) {
        this(source.getId(), source.getName());
    }

    /**
     * @param id   the Activity's id
     * @param name the Activity's name
     */
    public ActivityFormDTO(int id, String name) {
        this();
        setId(id);
        setName(name);
    }

    /**
     * @param db the UserDatabaseDTO to which this Activity belongs
     */
    public ActivityFormDTO(UserDatabaseDTO db) {
        this.databaseId = db.getId();
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int ownerUserId) {
        this.ownerUserId = ownerUserId;
    }


    /**
     * @return this Activity's id
     */
    @Override @JsonProperty @JsonView(DTOViews.Schema.class)
    public int getId() {
        return (Integer) get("id");
    }

    /**
     * Sets this Activity's id
     */
    public void setId(int id) {
        set("id", id);
    }

    /**
     * Sets this Activity's name
     */
    public void setName(String value) {
        set("name", value);
    }

    /**
     * @return this Activity's name
     */
    @Override @JsonProperty @JsonView(DTOViews.Schema.class)
    public String getName() {
        return get("name");
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public void setDatabase(UserDatabaseDTO database) {
        this.databaseId = database.getId();
        this.databaseName = database.getName();
    }


    @JsonProperty @JsonView(DTOViews.Schema.class)
    public int getPublished() {
        return get("published", 0);
    }

    public void setPublished(int published) {
        set("published", published);
    }

    /**
     * @return a list of this Activity's indicators
     */
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public List<IndicatorDTO> getIndicators() {
        return indicators;
    }

    /**
     * @param indicatorIds the ids of the indicators for which to search
     * @return true if this activity contains any of the provided indicators
     */
    public boolean containsAny(Set<Integer> indicatorIds) {
        for (IndicatorDTO indicator : getIndicators()) {
            if (indicatorIds.contains(indicator.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets this Activity's Indicator
     */
    public void setIndicators(List<IndicatorDTO> indicators) {
        this.indicators = indicators;
    }

    @JsonProperty @JsonView(DTOViews.Schema.class)
    public List<AttributeGroupDTO> getAttributeGroups() {
        return attributeGroups;
    }

    public void setAttributeGroups(List<AttributeGroupDTO> attributes) {
        this.attributeGroups = attributes;
    }

    public void setClassicView(boolean value) {
        set("classicView", value);
    }

    public boolean getClassicView() {
        return get("classicView", true);
    }

    @Override
    public ResourceId getFormClassId() {
        return CuidAdapter.activityFormClass(getId());
    }


    /**
     * Sets the ReportingFrequency of this Activity, either
     * <code>REPORT_ONCE</code> or <code>REPORT_MONTHLY</code>
     */
    public void setReportingFrequency(int frequency) {
        set("reportingFrequency", frequency);
    }

    /**
     * @return the ReportingFrequency of this Activity, either
     * <code>REPORT_ONCE</code> or <code>REPORT_MONTHLY</code>
     */
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public int getReportingFrequency() {
        return (Integer) get("reportingFrequency");
    }

    /**
     * Sets the id of the LocationType of the Location to which this Site
     * belongs.
     */
    public void setLocationTypeId(int locationId) {
        set("locationTypeId", locationId);

    }

    /**
     * @return the id of the LocationType of the Location to which this Site
     * belongs
     */

    public int getLocationTypeId() {
        return locationType.getId();
    }

    public void setLocationType(LocationTypeDTO locationType) {
        this.locationType = locationType;

        // for form binding. uck.
        set("locationTypeId", locationType.getId());
    }

    @JsonProperty @JsonView(DTOViews.Schema.class)
    public LocationTypeDTO getLocationType() {
        return locationType;
    }

    /**
     * Searches the list of Attributes for the AttributeDTO with the given id
     *
     * @return the AttributeDTO matching the given id, or null if no such
     * AttributeDTO was found.
     */
    public AttributeDTO getAttributeById(int id) {
        for (AttributeGroupDTO group : attributeGroups) {
            AttributeDTO attribute = SchemaDTO.getById(group.getAttributes(), id);
            if (attribute != null) {
                return attribute;
            }
        }
        return null;
    }

    public AttributeGroupDTO getAttributeGroupByAttributeId(int id) {
        for (AttributeGroupDTO group : attributeGroups) {
            AttributeDTO attribute = SchemaDTO.getById(group.getAttributes(), id);
            if (attribute != null) {
                return group;
            }
        }
        return null;
    }

    /**
     * Searches this Activity's list of Indicators for the IndicatorDTO with the
     * given id.
     *
     * @return the matching IndicatorDTO or null if nothing was found
     */
    public IndicatorDTO getIndicatorById(int indicatorId) {
        for (IndicatorDTO indicator : indicators) {
            if (indicator.getId() == indicatorId) {
                return indicator;
            }
        }
        return null;
    }

    /**
     * @return this Activity's category
     */
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public String getCategory() {
        return get("category");
    }

    /**
     * Sets this Activity's category
     */
    public void setCategory(String category) {
        if(category != null && category.trim().length() == 0) {
            category = null;
        }
        set("category", category);
    }

    public boolean hasCategory() {
        return !Strings.isNullOrEmpty(getCategory());
    }

    /**
     * Convenience method that creates a list of IndicatorGroups from this
     * Activity's list of Indicators, based on the Indicator's category
     * property.
     */
    public List<IndicatorGroup> groupIndicators() {
        List<IndicatorGroup> groups = new ArrayList<IndicatorGroup>();
        Map<String, IndicatorGroup> map = new HashMap<String, IndicatorGroup>();

        for (IndicatorDTO indicator : indicators) {
            String category = indicator.getCategory();
            IndicatorGroup group = map.get(category);
            if (group == null) {
                group = new IndicatorGroup(category);
                group.setActivityId(this.getId());
                map.put(category, group);
                groups.add(group);
            }
            group.addIndicator(indicator);
        }
        return groups;
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    /**
     * @return The list of admin levels that can be set for this Activity's
     * LocationType.
     */
    public List<AdminLevelDTO> getAdminLevels() {
        return locationType.getAdminLevels();
    }

    /**
     * Searches this Activity's list of AttributeGroups for an AttributeGroupDTO
     * with the given id
     *
     * @return the matching AttributeGroupDTO or null if there are no matches
     */
    public AttributeGroupDTO getAttributeGroupById(int id) {
        for (AttributeGroupDTO group : attributeGroups) {
            if (group.getId() == id) {
                return group;
            }
        }
        return null;
    }

    public AttributeGroupDTO getAttributeGroupByName(String attributeName) {
        for (AttributeGroupDTO group : attributeGroups) {
            if (group.getName().equalsIgnoreCase(attributeName)) {
                return group;
            }
        }
        return null;
    }

    @Override
    public String getKey() {
        return "act" + getId();
    }

    public void setLockedPeriods(Set<LockedPeriodDTO> lockedPeriods) {
        this.lockedPeriods = lockedPeriods;
    }

    @Override @JsonProperty @JsonView(DTOViews.Schema.class)
    public Set<LockedPeriodDTO> getLockedPeriods() {
        return lockedPeriods;
    }

    @Override
    public Set<LockedPeriodDTO> getEnabledLockedPeriods() {
        Set<LockedPeriodDTO> enabled = Sets.newHashSet();

        for (LockedPeriodDTO lockedPeriod : getLockedPeriods()) {
            if (lockedPeriod.isEnabled()) {
                enabled.add(lockedPeriod);
            }
        }

        return enabled;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public boolean isEditAllowed() {
        return editAllowed;
    }

    public void setEditAllowed(boolean editAllowed) {
        this.editAllowed = editAllowed;
    }

    public boolean isEditAllAllowed() {
        return editAllAllowed;
    }

    public void setEditAllAllowed(boolean editAllAllowed) {
        this.editAllAllowed = editAllAllowed;
    }

    public int getCurrentPartnerId() {
        return currentPartnerId;
    }

    public void setCurrentPartnerId(int currentPartnerId) {
        this.currentPartnerId = currentPartnerId;
    }

    public boolean isAllowedToEdit(SiteDTO site) {
        return editAllAllowed || site.getPartnerId() == currentPartnerId;
    }


    public boolean isDesignAllowed() {
        return designAllowed;
    }

    public void setDesignAllowed(boolean designAllowed) {
        this.designAllowed = designAllowed;
    }

    public List<ProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }

    public Extents getBounds() {
        return locationType.getCountryBounds();
    }

    /**
     *
     * @return the list of allowable values for the partner
     * field for the requesting user
     */
    public List<PartnerDTO> getPartnerRange() {
        return partners;
    }

    public void setPartnerRange(List<PartnerDTO> partners) {
        this.partners = partners;
    }

    public LockedPeriodSet getLockedPeriodSet() {
        return new LockedPeriodSet(this);
    }

    public ProjectDTO getProjectById(int projectId) {
        for(ProjectDTO project : projects) {
            if(project.getId() == projectId) {
                return project;
            }
        }
        return null;
    }

    public PartnerDTO getPartnerById(int partnerId) {
        for(PartnerDTO partner : partners) {
            if(partner.getId() == partnerId) {
                return partner;
            }
        }
        return null;
    }
}
