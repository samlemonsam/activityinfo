package org.activityinfo.server.command.handler.crud;

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

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.server.command.handler.PermissionOracle;
import org.activityinfo.server.command.handler.json.JsonHelper;
import org.activityinfo.server.database.hibernate.dao.ActivityDAO;
import org.activityinfo.server.database.hibernate.dao.UserDatabaseDAO;
import org.activityinfo.server.database.hibernate.entity.Activity;
import org.activityinfo.server.database.hibernate.entity.LocationType;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.database.hibernate.entity.UserDatabase;

import javax.persistence.EntityManager;
import java.util.Date;

import static org.activityinfo.legacy.shared.util.StringUtil.truncate;

public class ActivityPolicy implements EntityPolicy<Activity> {

    private final EntityManager em;
    private final ActivityDAO activityDAO;
    private final UserDatabaseDAO databaseDAO;

    @Inject
    public ActivityPolicy(EntityManager em, ActivityDAO activityDAO, UserDatabaseDAO databaseDAO) {
        this.em = em;
        this.activityDAO = activityDAO;
        this.databaseDAO = databaseDAO;
    }

    @Override
    public Object create(User user, PropertyMap properties) {

        UserDatabase database = getDatabase(properties);
        PermissionOracle.using(em).assertDesignPrivileges(database, user);

        // create the entity
        Activity activity = new Activity();
        activity.setDatabase(database);
        activity.setSortOrder(calculateNextSortOrderIndex(database.getId()));
        activity.setLocationType(getLocationType(properties));
        
        
        // activity should be classic by default
        activity.setClassicView(true);

        applyProperties(activity, properties);

        activityDAO.persist(activity);

        return activity.getId();
    }

    public Activity persist(Activity activity) {
        activityDAO.persist(activity);
        return activity;
    }

    @Override
    public void update(User user, Object entityId, PropertyMap changes) {
        Activity activity = em.find(Activity.class, entityId);
        
        PermissionOracle.using(em).assertDesignPrivileges(activity.getDatabase(), user);
       
        activity.incrementSchemaVersion();

        applyProperties(activity, changes);
    }

    private UserDatabase getDatabase(PropertyMap properties) {
        return databaseDAO.findById(properties.getRequiredInt("databaseId"));
    }

    private LocationType getLocationType(PropertyMap properties) {
        return em.getReference(LocationType.class, properties.getRequiredInt("locationTypeId"));
    }

    private Integer calculateNextSortOrderIndex(int databaseId) {
        Integer maxSortOrder = activityDAO.queryMaxSortOrder(databaseId);
        return maxSortOrder == null ? 1 : maxSortOrder + 1;
    }

    private void applyProperties(Activity activity, PropertyMap changes) {
        if (changes.containsKey("name")) {
            String name = truncate((String) changes.get("name"));

            activity.setName(name);

            String json = JsonHelper.readJson(activity);

            updateFormClass(activity, name, json);
        }

        if (changes.containsKey("locationTypeId")) {
            LocationType location = em.find(LocationType.class, changes.get("locationTypeId"));
            if (location != null) {
                activity.setLocationType(location);
            }
        }

        if (changes.containsKey("locationType")) {
            activity.setLocationType(em.getReference(LocationType.class,
                    ((LocationTypeDTO) changes.get("locationType")).getId()));
        }

        if (changes.containsKey("category")) {
            String category = Strings.nullToEmpty((String) changes.get("category")).trim();
            activity.setCategory(truncate(Strings.emptyToNull(category)));
        }

        if (changes.containsKey("mapIcon")) {
            activity.setMapIcon((String) changes.get("mapIcon"));
        }

        if (changes.containsKey("reportingFrequency")) {
            activity.setReportingFrequency((Integer) changes.get("reportingFrequency"));
        }

        if (changes.containsKey("published")) {
            activity.setPublished((Integer) changes.get("published"));
        }

        if (changes.containsKey("classicView")) {
            activity.setClassicView((Boolean) changes.get("classicView"));
        }

        if (changes.containsKey("sortOrder")) {
            activity.setSortOrder((Integer) changes.get("sortOrder"));
        }

        activity.getDatabase().setLastSchemaUpdate(new Date());
    }

    private void updateFormClass(Activity activity, String name, String json) {
        if (!Strings.isNullOrEmpty(json)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(json).getAsJsonObject();
            FormClass formClass = FormClass.fromJson(jsonObject);
            formClass.setLabel(name);

            String updatedJson = formClass.toJsonString();

            JsonHelper.updateWithJson(activity, updatedJson);
        }
    }
}
