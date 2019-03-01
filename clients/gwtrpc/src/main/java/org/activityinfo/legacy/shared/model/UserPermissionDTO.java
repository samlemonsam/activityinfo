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
import org.activityinfo.legacy.shared.validation.Required;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object (DTO) for a single user's permission on a database.
 *
 * @author Alex Bertram
 */
@JsonAutoDetect(JsonMethod.NONE)
public final class UserPermissionDTO extends BaseModelData implements DTO {

    private List<PartnerDTO> partners = new ArrayList<>(0);

    private boolean hasFolderLimitation = false;
    private List<FolderDTO> folders = new ArrayList<>(0);

    public UserPermissionDTO() {
        setAllowView(true);
        setAllowViewAll(false);
        setAllowCreate(false);
        setAllowCreateAll(false);
        setAllowEdit(false);
        setAllowEditAll(false);
        setAllowDelete(false);
        setAllowDeleteAll(false);
        setAllowManageUsers(false);
        setAllowManageAllUsers(false);
        setAllowDesign(false);
        setAllowExport(false);
    }

    public void setName(String value) {
        set("name", value);
    }

    /**
     * Returns the User's name.
     *
     * @return the user's name
     */
    @Required
    @Length(max = 50)
    @JsonProperty
    public String getName() {
        return get("name");
    }

    @JsonProperty
    @Length(max = 100)
    public String getOrganization() {
        return get("organization");
    }

    public void setOrganization(String organization) {
        set("organization", organization);
    }

    @JsonProperty
    @Length(max = 100)
    public String getJobtitle() {
        return get("jobtitle");
    }

    public void setJobtitle(String jobtitle) {
        set("jobtitle", jobtitle);
    }

    public void setEmail(String value) {
        set("email", value);
    }

    /**
     * Returns the User's email
     *
     * @return the User's email
     */
    @Required
    @Email
    @JsonProperty
    @Length(max = 75)
    public String getEmail() {
        return get("email");
    }

    public void setAllowView(boolean value) {
        set("allowView", value);
    }

    public void setAllowDesign(boolean value) {
        set("allowDesign", value);
    }

    @JsonProperty
    public boolean getAllowDesign() {
        return (Boolean) get("allowDesign");
    }

    @JsonProperty
    public boolean getAllowView() {
        return (Boolean) get("allowView");
    }

    public void setAllowViewAll(boolean value) {
        set("allowViewAll", value);
    }

    @JsonProperty
    public boolean getAllowViewAll() {
        return (Boolean) get("allowViewAll");
    }

    public void setAllowCreate(boolean value) {
        set("allowCreate", value);
    }

    @JsonProperty
    public boolean getAllowCreate() {
        return (Boolean) get("allowCreate");
    }

    public void setAllowCreateAll(boolean value) {
        set("allowCreateAll", value);
    }

    @JsonProperty
    public boolean getAllowCreateAll() {
        return (Boolean) get("allowCreateAll");
    }

    public void setAllowEdit(boolean value) {
        set("allowEdit", value);
    }

    @JsonProperty
    public boolean getAllowEdit() {
        return (Boolean) get("allowEdit");
    }

    public void setAllowEditAll(boolean value) {
        set("allowEditAll", value);
    }

    @JsonProperty
    public boolean getAllowEditAll() {
        return (Boolean) get("allowEditAll");
    }

    public void setAllowDelete(boolean value) {
        set("allowDelete", value);
    }

    @JsonProperty
    public boolean getAllowDelete() {
        return (Boolean) get("allowDelete");
    }

    public void setAllowDeleteAll(boolean value) {
        set("allowDeleteAll", value);
    }

    @JsonProperty
    public boolean getAllowDeleteAll() {
        return (Boolean) get("allowDeleteAll");
    }

    @JsonProperty
    public boolean getAllowManageUsers() {
        return (Boolean) get("allowManageUsers");
    }

    public void setAllowManageUsers(boolean allowManageUsers) {
        set("allowManageUsers", allowManageUsers);
    }

    @JsonProperty
    public boolean getAllowManageAllUsers() {
        return (Boolean) get("allowManageAllUsers");
    }

    public void setAllowManageAllUsers(boolean allowManageAll) {
        set("allowManageAllUsers", allowManageAll);
    }

    public void setAllowExport(boolean value) {
        set("allowExport", value);
    }

    @JsonProperty
    public boolean getAllowExport() {
        return (Boolean) get("allowExport");
    }

    public List<PartnerDTO> getPartners() {
        return partners;
    }

    public List<Integer> getPartnerIds() {
        return partners.stream().map(PartnerDTO::getId).collect(Collectors.toList());
    }

    public void setPartners(List<PartnerDTO> partners) {
        this.partners = partners;
    }

    public void addPartner(PartnerDTO partner) {
        this.partners.add(partner);
    }

    public void addPartners(Collection<PartnerDTO> partners) {
        this.partners.addAll(partners);
    }

    /**
     *
     * @return a list of folders to which the user has access
     */
    public List<FolderDTO> getFolders() {
        return folders;
    }

    public void setFolders(List<FolderDTO> folders) {
        this.folders = folders;
    }

    public void setFolderLimitation(boolean folderLimitation) {
        this.hasFolderLimitation = folderLimitation;
    }

    // Check to see whether validation flag is set
    public boolean hasFolderLimitation() {
        return hasFolderLimitation;
    }

    public String toString() {
        return "{email=" + getEmail() +
                ",name=" + getName() +
                ",partners=" + getPartners() +
                ",view=" + getAllowView() +
                ",viewAll=" + getAllowViewAll() +
                ",create=" + getAllowCreate() +
                ",createAll=" + getAllowCreateAll() +
                ",edit=" + getAllowEdit() +
                ",editAll=" + getAllowEditAll() +
                ",delete=" + getAllowDelete() +
                ",deleteAll=" + getAllowDeleteAll() +
                ",manageUsers=" + getAllowManageUsers() +
                ",manageAllUsers=" + getAllowManageAllUsers() +
                ",exportRecords=" + getAllowExport() +
                "}";
    }
}
