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
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.legacy.shared.model.LockedPeriodDTO.HasLockedPeriod;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * UserDatabase Data Transfer Object.
 *
 * @author Alex Bertram
 */
@JsonAutoDetect(JsonMethod.NONE)
public final class UserDatabaseDTO extends BaseModelData implements EntityDTO, HasLockedPeriod, ProvidesKey {

    public static final int MAX_NAME_LENGTH = 255;

    public static final String ENTITY_NAME = "UserDatabase";

    private CountryDTO country;
    private List<ActivityDTO> activities = new ArrayList<>(0);
    private Set<LockedPeriodDTO> lockedPeriods = new HashSet<>(0);
    private List<ProjectDTO> projects = new ArrayList<>(0);
    private List<FolderDTO> folders = new ArrayList<>(0);

    private List<PartnerDTO> databasePartners = new ArrayList<>(0);
    private List<PartnerDTO> assignedPartners = new ArrayList<>(0);

    private boolean hasFolderLimitation = false;
    private boolean hasPendingTransfer = false;
    private boolean suspended;

    public UserDatabaseDTO() {
    }

    public UserDatabaseDTO(int id, String name) {
        setId(id);
        setName(name);
    }

    /**
     * @return this UserDatabase's id
     */
    @Override @JsonProperty @JsonView(DTOViews.List.class)
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
    public List<ActivityDTO> getActivities() {
        return activities;
    }

    /**
     * @param activities sets the list of Activities in this UserDatabase
     */
    public void setActivities(List<ActivityDTO> activities) {
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
    public List<PartnerDTO> getDatabasePartners() {
        return databasePartners;
    }

    /**
     * Sets the list of Partners who belong to this UserDatabase
     */
    public void setDatabasePartners(List<PartnerDTO> databasePartners) {
        this.databasePartners = databasePartners;
    }

    public void addDatabasePartner(PartnerDTO databasePartner) {
        this.databasePartners.add(databasePartner);
    }

    /**
     * @return the Partner(s) of the UserDatabase to which the client belongs
     */
    public List<PartnerDTO> getAssignedPartners() {
        return assignedPartners;
    }

    public void setAssignedPartners(List<PartnerDTO> assignedPartners) {
        this.assignedPartners = assignedPartners;
    }

    public void addAssignedPartner(PartnerDTO assignedPartner) {
        this.assignedPartners.add(assignedPartner);
    }

    public boolean hasAssignedPartners() {
        return !assignedPartners.isEmpty();
    }

    public Optional<PartnerDTO> getDefaultPartner() {
        return getDefaultPartner(getDatabasePartners());
    }

    public static Optional<PartnerDTO> getDefaultPartner(Collection<PartnerDTO> partners) {
        for (PartnerDTO partner : partners) {
            if (PartnerDTO.DEFAULT_PARTNER_NAME.equals(partner.getName())) {
                return Optional.of(partner);
            }
        }
        return Optional.absent();
    }

    public List<FolderDTO> getFolders() {
        return folders;
    }


    public String getBillingAccountName() {
        return get("billingAccountName");
    }

    public void setBillingAccountName(String name) {
        set("billingAccountName", name);
    }

    public String getAccountEndDate() {
        return get("accountEndDate");
    }

    public void setAccountEndDate(String dateString) {
        set("accountEndDate", dateString);
    }

    @JsonProperty @JsonView(DTOViews.Schema.class)
    public List<ProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
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

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    /**
     * Searches this UserDatabase's list of Partners for the PartnerDTO with the
     * given id.
     *
     * @return the matching PartnerDTO or null if no matches
     */
    public PartnerDTO getPartnerById(int id) {
        for (PartnerDTO partner : getDatabasePartners()) {
            if (partner.getId() == id) {
                return partner;
            }
        }
        return null;
    }

    public ActivityDTO getActivityById(int id) {
        for (ActivityDTO activity : getActivities()) {
            if (activity.getId() == id) {
                return activity;
            }
        }
        return null;
    }

    @Override
    public String getKey() {
        return "db" + getId();
    }

    public void setLockedPeriods(Set<LockedPeriodDTO> lockedPeriods) {
        this.lockedPeriods = lockedPeriods;
    }

    @Override @JsonProperty @JsonView(DTOViews.Schema.class)
    public Set<LockedPeriodDTO> getLockedPeriods() {
        return lockedPeriods;
    }

    public ProjectDTO getProjectById(int value) {
        for (ProjectDTO project : getProjects()) {
            if (value == project.getId()) {
                return project;
            }
        }

        return null;
    }

    public FolderDTO getFolderById(int folderId) {
        for (FolderDTO folder : folders) {
            if(folder.getId() == folderId) {
                return folder;
            }
        }
        return null;
    }

    @JsonIgnore
    @Override
    public Map<String, Object> getProperties() {
        return super.getProperties();
    }

    @JsonIgnore
    @Override
    public Collection<String> getPropertyNames() {
        return super.getPropertyNames();
    }

    public boolean hasPendingTransfer() {
        return hasPendingTransfer;
    }

    public void setHasPendingTransfer(boolean hasPendingTransfer) {
        this.hasPendingTransfer = hasPendingTransfer;
    }

    /////////////////////////////////////////////////// PERMISSIONS ///////////////////////////////////////////////////

    /**
     * Sets the permission of the current user to view all partner's data in
     * this UserDatabase.
     */
    public void setViewAllAllowed(boolean value) {
        set("viewAllAllowed", value);
    }

    /**
     * @return true if the client receiving the DTO is authorized to view data
     * from all partners in this UserDatabase.
     */
    public boolean isViewAllAllowed() {
        return get("viewAllAllowed", false);
    }

    /**
     * Sets the permission of the current user to create data on behalf of the
     * Partner in this UserDatabase to which the current user belongs.
     */
    public void setCreateAllowed(boolean allowed) {
        set("createAllowed", allowed);
    }

    /**
     * @return true if the client receiving the DTO is authorized to create data
     * for their Partner in this UserDatabase
     */
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public boolean isCreateAllowed() {
        return get("createAllowed", false);
    }

    /**
     * Sets the permission of the current user to create data in this UserDatabase
     * on behalf of all partners.
     */
    public void setCreateAllAllowed(boolean value) {
        set("createAllAllowed", value);
    }

    /**
     * @return true if the client receiving the DTO is authorized to create data
     * for all Partners in this UserDatabase
     */
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public boolean isCreateAllAllowed() {
        return get("createAllAllowed", false);
    }


    /**
     * Sets the permission of the current user to edit data on behalf of the
     * Partner in this UserDatabase to which the current user belongs.
     */
    public void setEditAllowed(boolean allowed) {
        set("editAllowed", allowed);
    }

    /**
     * @return true if the client receiving the DTO is authorized to edit data
     * for their Partner in this UserDatabase
     */
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public boolean isEditAllowed() {
        return get("editAllowed", false);
    }

    /**
     * Sets the permission of the current user to edit data in this UserDatabase
     * on behalf of all partners.
     */
    public void setEditAllAllowed(boolean value) {
        set("editAllAllowed", value);
    }

    /**
     * @return true if the client receiving the DTO is authorized to edit data
     * for all Partners in this UserDatabase
     */
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public boolean isEditAllAllowed() {
        return get("editAllAllowed", false);
    }

    /**
     * Sets the permission of the current user to delete data on behalf of the
     * Partner in this UserDatabase to which the current user belongs.
     */
    public void setDeleteAllowed(boolean allowed) {
        set("deleteAllowed", allowed);
    }

    /**
     * @return true if the client receiving the DTO is authorized to delete data
     * for their Partner in this UserDatabase
     */
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public boolean isDeleteAllowed() {
        return get("deleteAllowed", false);
    }

    /**
     * Sets the permission of the current user to delete data in this UserDatabase
     * on behalf of all partners.
     */
    public void setDeleteAllAllowed(boolean value) {
        set("deleteAllAllowed", value);
    }

    /**
     * @return true if the client receiving the DTO is authorized to delete data
     * for all Partners in this UserDatabase
     */
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public boolean isDeleteAllAllowed() {
        return get("deleteAllAllowed", false);
    }


    /**
     * Sets the permission of the current user to design this UserDatabase.
     */
    public void setDesignAllowed(boolean allowed) {
        set("designAllowed", allowed);
    }

    /**
     * @return true if the client receiving the DTO is authorized to design
     * at least some forms in this database.
     */
    @JsonProperty @JsonView(DTOViews.Schema.class)
    public boolean isDesignAllowed() {
        return get("designAllowed", false);
    }

    public boolean isDatabaseDesignAllowed() {
        return get("databaseDesignAllowed", false);
    }

    public void setDatabaseDesignAllowed(boolean value) {
        set("databaseDesignAllowed", value);
    }

    /**
     * @return true if current user is allowed to make changes to user
     * permissions on behalf of the Partner to which they belong
     */
    public boolean isManageUsersAllowed() {
        return get("manageUsersAllowed", false);
    }

    /**
     * Sets the permission of the current user to make changes to user
     * permissions on behalf of the Partner to which they belong in this
     * UserDatabase.
     */
    public void setManageUsersAllowed(boolean allowed) {
        set("manageUsersAllowed", allowed);
    }

    /**
     * @return true if the current user is allowed to make changes to user
     * permissions on behalf of all Partners in this UserDatabase
     */
    public boolean isManageAllUsersAllowed() {
        return get("manageAllUsersAllowed", false);
    }

    /**
     * Sets the permission of the current user to modify user permissions for
     * this UserDatabase on behalf of all Partners in this UserDatabase
     */
    public void setManageAllUsersAllowed(boolean allowed) {
        set("manageAllUsersAllowed", allowed);
    }

    public boolean isExportAllowed() {
        return get("exportAllowed", false);
    }

    public void setExportAllowed(boolean allowed) {
        set("exportAllowed", allowed);
    }

    public boolean isVisible(@NotNull ActivityDTO activity) {
        if (!hasFolderLimitation) {
            return true;
        } else if (activity.getFolder() == null) {
            return false;
        } else {
            return folders.contains(activity.getFolder());
        }
    }

    public boolean isVisible(@NotNull FolderDTO folder) {
        if (!hasFolderLimitation) {
            return true;
        } else {
            return folders.contains(folder);
        }
    }

    /**
     * @return true if current user is allowed to make changes to partners on this database
     */
    public boolean isManagePartnersAllowed() {
        // User must have Design Permissions, and have ALL folder access
        return isDesignAllowed() && !hasFolderLimitation;
    }

    /**
     * @return true if current user is allowed to make changes to projects on this database
     */
    public boolean isManageProjectsAllowed() {
        // User must have Design Permissions, and have ALL folder access
        return isDesignAllowed() && !hasFolderLimitation;
    }

    public boolean canManageUser(UserPermissionDTO user) {
        if (isManageAllUsersAllowed()) {
            return true;
        }
        if (isManageUsersAllowed()) {
            return getAllowablePartners().containsAll(user.getPartners());
        }
        return false;
    }

    public boolean hasFolderLimitation() {
        return hasFolderLimitation;
    }

    public void setFolderLimitation(boolean hasFolderLimitation) {
        this.hasFolderLimitation = hasFolderLimitation;
    }

    private boolean isFolderSubset(List<FolderDTO> folders) {
        return getFolders().containsAll(folders);
    }

    public List<PartnerDTO> getAllowablePartners() {
        Set<PartnerDTO> result = Sets.newHashSet();
        Optional<PartnerDTO> defaultPartner = getDefaultPartner();

        if (defaultPartner.isPresent()) {
            result.add(defaultPartner.get());
        }

        if (isEditAllAllowed()) {
            result.addAll(getDatabasePartners());
        } else if (hasAssignedPartners()) {
            result.addAll(getAssignedPartners());
        } else {
            // if the user has no specific rights, they may not
            // have any options to set the partner
        }
        return Lists.newArrayList(result);
    }

    public boolean canAssignFolder(int folderId, UserPermissionDTO user) {
        // Check if database user can manage any other users
        if (!isManageUsersAllowed() && !isManageAllUsersAllowed()) {
            return false;
        // Check if database user has any folder limitations - if none, can assign any folder
        } else if (!hasFolderLimitation()) {
            return true;
        // Check if user has "All" folder permissions - cannot change folder on root users if we are not one
        } else if (!user.hasFolderLimitation()) {
            return false;
        } else {
            // Else, check if this folder is assignable by matching to a folder on the database user's list
            for (FolderDTO folder : folders) {
                if (folder.getId() == folderId) {
                    return true;
                }
            }
            // If no match - return false
            return false;
        }
    }

    public boolean canTransferDatabase() {
        return getAmOwner() && !hasPendingTransfer;
    }

    public boolean canCancelTransfer() {
        return getAmOwner() && hasPendingTransfer;
    }

    /**
     * Compare the set of permissions of the database user, and the given user.
     * Will return false if the user has any one permission the database user doesn't, and return true otherwise
     */
    public boolean hasGreaterPermissions(UserPermissionDTO user) {
        if (user.getAllowCreate() == Boolean.TRUE && !isCreateAllowed()) {
            return false;
        }
        if (user.getAllowEdit() == Boolean.TRUE && !isEditAllowed()) {
            return false;
        }
        if (user.getAllowDelete() == Boolean.TRUE && !isDeleteAllowed()) {
            return false;
        }
        if (user.getAllowViewAll() == Boolean.TRUE && !isViewAllAllowed()) {
            return false;
        }
        if (user.getAllowCreateAll() == Boolean.TRUE && !isCreateAllAllowed()) {
            return false;
        }
        if (user.getAllowEditAll() == Boolean.TRUE && !isEditAllAllowed()) {
            return false;
        }
        if (user.getAllowDeleteAll() == Boolean.TRUE && !isDeleteAllAllowed()) {
            return false;
        }
        if (user.getAllowDesign() == Boolean.TRUE && !isDesignAllowed()) {
            return false;
        }
        if (user.getAllowExport() == Boolean.TRUE && !isExportAllowed()) {
            return false;
        }
        if (user.getAllowManageUsers() == Boolean.TRUE && !isManageUsersAllowed()) {
            return false;
        }
        if (user.getAllowManageAllUsers() == Boolean.TRUE && !isManageAllUsersAllowed()) {
            return false;
        }
        return true;
    }

    public boolean canGivePermission(PermissionType permissionType, UserPermissionDTO user) {
        if (permissionType == null) {
            return false;
        }

        // Owners should be able to give permissions of any type to any user
        if (getAmOwner()) {
            return true;
        }

        // Check the database user has an identical or greater permission set than user
        if (user != null && !this.hasGreaterPermissions(user)) {
            return false;
        }

        switch (permissionType) {
            // Always allowed to give permissions on the same partner, provided the database user has them
            case VIEW:
            case CREATE:
            case EDIT:
            case DELETE:
            case MANAGE_USERS:
            case DESIGN:
            case EXPORT_RECORDS:
                return isAllowed(permissionType, user);
            // Only allowed to give permissions on all partners, if database user can manage users for all partners
            case VIEW_ALL:
            case CREATE_ALL:
            case EDIT_ALL:
            case DELETE_ALL:
            case MANAGE_ALL_USERS:
                return isAllowed(permissionType, user) && isManageAllUsersAllowed();
            default:
                return false;
        }
    }

    public boolean isAllowed(PermissionType permissionType, UserPermissionDTO user) {
        if (permissionType == null) {
            return false;
        }

        // If no user to check against - check basic permission
        if (user == null) {
            return checkBasicPermission(permissionType);
        } else {
            // Check if database user has basic permission
            if (!checkBasicPermission(permissionType)) {
                return false;
            // Check if database user has partner permissions
            } else if (!getAllowablePartners().containsAll(user.getPartners())) {
                return false;
            // Check if database user has any folder limitations - if none, then allowed
            } else if (!hasFolderLimitation()) {
                return true;
            // Check if database user has identical or greater set of folder permissions - if true, then allowed
            } else if (isFolderSubset(user.getFolders())) {
                return true;
            // Otherwise - false
            } else {
                return false;
            }
        }
    }

    private boolean checkBasicPermission(PermissionType permissionType) {
        switch (permissionType) {
            case VIEW:
                return true;                    // always allowed to view on partner/folder levels
            case VIEW_ALL:
                return isViewAllAllowed();
            case CREATE:
                return isCreateAllowed();
            case CREATE_ALL:
                return isCreateAllAllowed();
            case EDIT:
                return isEditAllowed();
            case EDIT_ALL:
                return isEditAllAllowed();
            case DELETE:
                return isDeleteAllowed();
            case DELETE_ALL:
                return isDeleteAllAllowed();
            case MANAGE_USERS:
                return isManageUsersAllowed();
            case MANAGE_ALL_USERS:
                return isManageAllUsersAllowed();
            case DESIGN:
                return isDesignAllowed();
            case EXPORT_RECORDS:
                return isExportAllowed();
            default:
                return false;
        }
    }

}
