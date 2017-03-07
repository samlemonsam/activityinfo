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

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuriyz on 10/07/2014.
 */
public class ActivityCategory extends BaseModelData implements ProvidesKey {

    private List<ActivityDTO> activities = new ArrayList<ActivityDTO>();
    private int databaseId;

    public ActivityCategory() {
    }

    public ActivityCategory(int databaseId, String name) {
        this.databaseId = databaseId;
        set("name", name);
    }


    /**
     * Returns the name of the ActivityCategory;
     *
     * @return the name of the ActivityCategory
     */
    public String getName() {
        return get("name");
    }

    public List<ActivityDTO> getActivities() {
        return activities;
    }

    public ActivityCategory addActivity(ActivityDTO activityDTO) {
        activities.add(activityDTO);
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityCategory that = (ActivityCategory) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public String getKey() {
        return "activity_category_" + databaseId + "_" + getName();
    }
}
