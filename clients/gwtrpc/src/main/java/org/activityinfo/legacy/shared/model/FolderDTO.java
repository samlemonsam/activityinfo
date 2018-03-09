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

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuriyz on 10/07/2014.
 */
public class FolderDTO extends BaseModelData implements ProvidesKey, EntityDTO {


    private List<ActivityDTO> activities = new ArrayList<ActivityDTO>();

    public FolderDTO() {
    }

    public FolderDTO(int databaseId, String name) {
        setDatabaseId(databaseId);
        setName(name);
    }

    public FolderDTO(int databaseId, int folderId, String name) {
        setId(folderId);
        setDatabaseId(databaseId);
        setName(name);
    }

    public int getId() {
        return get("id");
    }

    public void setId(int id) {
        set("id", id);
    }

    /**
     * Returns the name of the ActivityCategory;
     *
     * @return the name of the ActivityCategory
     */
    public String getName() {
        return get("name");
    }


    public void setName(String name) {
        set("name", name);
    }

    public void setDatabaseId(int databaseId) {
        set("databaseId", databaseId);
    }

    public int getDatabaseId() {
        return get("databaseId");
    }

    public List<ActivityDTO> getActivities() {
        return activities;
    }

    public FolderDTO addActivity(ActivityDTO activityDTO) {
        activities.add(activityDTO);
        return this;
    }


    @Override
    public String getEntityName() {
        return "Folder";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderDTO that = (FolderDTO) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public String getKey() {
        return "activity_category_" + getDatabaseId() + "_" + getName();
    }
}
