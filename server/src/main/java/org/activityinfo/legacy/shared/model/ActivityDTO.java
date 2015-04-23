package org.activityinfo.legacy.shared.model;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.activityinfo.legacy.shared.reports.util.mapping.Extents;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ActivityDTO extends BaseModelData implements EntityDTO, ProvidesKey,
        LockedPeriodDTO.HasLockedPeriod, IsActivityDTO {

    public static final String ENTITY_NAME = "Activity";

    private UserDatabaseDTO database;

    private Set<LockedPeriodDTO> lockedPeriods = new HashSet<LockedPeriodDTO>(0);

    // to ensure serializer
    private Published _published;
    private LocationTypeDTO locationType;

    private int version;

    public ActivityDTO() {
        setReportingFrequency(ActivityFormDTO.REPORT_ONCE);
    }

    /**
     * Constructs a DTO with the given properties
     */
    public ActivityDTO(Map<String, Object> properties) {
        super(properties);
    }

    /**
     * Creates a shallow clone
     *
     * @param model
     */
    public ActivityDTO(ActivityDTO model) {
        super(model.getProperties());
        this.database = model.database;
        this.setLocationType(model.getLocationType());
    }

    public ActivityDTO(UserDatabaseDTO db, ActivityFormDTO form) {
        setId(form.getId());
        setDatabase(db);
        setName(form.getName());
        setLocationType(form.getLocationType());
        setReportingFrequency(form.getReportingFrequency());
        setCategory(form.getCategory());
        setClassicView(form.getClassicView());
        setPublished(form.getPublished());
    }

    /**
     * @param id   the Activity's id
     * @param name the Activity's name
     */
    public ActivityDTO(int id, String name) {
        this();
        setId(id);
        setName(name);
    }

    /**
     * @param db the UserDatabaseDTO to which this Activity belongs
     */
    public ActivityDTO(UserDatabaseDTO db) {
        setDatabase(db);
    }

    @Override
    public ResourceId getResourceId() {
        return CuidAdapter.activityFormClass(getId());
    }

    /**
     * @return this Activity's id
     */
    @Override @JsonProperty
    @JsonView(DTOViews.Schema.class)
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

    /**
     * @return the database to which this Activity belongs
     */
    public UserDatabaseDTO getDatabase() {
        return database;
    }

    public int getDatabaseId() {
        return database.getId();
    }

    /**
     * Sets the database to which this Activity belongs
     */
    public void setDatabase(UserDatabaseDTO database) {
        this.database = database;
    }

    @JsonProperty @JsonView(DTOViews.Schema.class)
    public int getPublished() {
        return (Integer) get("published");
    }

    public void setPublished(int published) {
        set("published", published);
    }

    public void setClassicView(boolean value) {
        set("classicView", value);
    }

    public boolean getClassicView() {
        return get("classicView");
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
        if(locationType != null) {
            set("locationTypeId", locationType.getId());
        }
    }

    @JsonProperty @JsonView(DTOViews.Schema.class)
    public LocationTypeDTO getLocationType() {
        return locationType;
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



    @Override
    public String getKey() {
        return "act" + getId();
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
        return database.getName();
    }

    public boolean isEditAllowed() {
        return database.isEditAllowed();
    }

    public boolean isAllowedToEdit(SiteDTO site) {
        return database.isAllowedToEdit(site);
    }


    public boolean isDesignAllowed() {
        return database.isEditAllowed();
    }

    public List<ProjectDTO> getProjects() {
        return database.getProjects();
    }

    public CountryDTO getCountry() {
        return database.getCountry();
    }

    public Extents getBounds() {
        return database.getCountry().getBounds();
    }

    public ResourceId getFormClassId() {
        return CuidAdapter.activityFormClass(getId());
    }
}
