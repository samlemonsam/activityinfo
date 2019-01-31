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
package org.activityinfo.server.command.handler.crud;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.activityinfo.json.JsonParser;
import org.activityinfo.json.JsonValue;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.command.handler.json.JsonHelper;
import org.activityinfo.server.database.hibernate.dao.ActivityDAO;
import org.activityinfo.server.database.hibernate.dao.UserDatabaseDAO;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.store.query.UsageTracker;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.activityinfo.legacy.shared.model.ActivityDTO.*;
import static org.activityinfo.legacy.shared.util.StringUtil.truncate;

public class ActivityPolicy implements EntityPolicy<Activity> {

    private static final Logger LOGGER = Logger.getLogger(ActivityPolicy.class.getName());

    private final EntityManager em;
    private final DatabaseProvider databaseProvider;
    private final ActivityDAO activityDAO;
    private final UserDatabaseDAO databaseDAO;

    @Inject
    public ActivityPolicy(EntityManager em,
                          DatabaseProvider databaseProvider,
                          ActivityDAO activityDAO,
                          UserDatabaseDAO databaseDAO) {
        this.em = em;
        this.databaseProvider = databaseProvider;
        this.activityDAO = activityDAO;
        this.databaseDAO = databaseDAO;
    }

    @Override
    public Object create(User user, PropertyMap properties) {

        Database database = getDatabase(properties);
        Optional<UserDatabaseMeta> databaseMeta = getDatabaseMeta(properties, user);

        assertCreateFormRights(databaseMeta);

        // create the entity
        Activity activity = new Activity();
        activity.setDatabase(database);
        activity.setSortOrder(calculateNextSortOrderIndex(database.getId()));
        activity.setLocationType(getLocationType(properties));
        
        
        // activity should be classic by default
        activity.setClassicView(true);

        applyProperties(activity, properties);

        activityDAO.persist(activity);

        UsageTracker.track(user.getId(), "create_activity", database.getResourceId());

        return activity.getId();
    }

    private void assertCreateFormRights(Optional<UserDatabaseMeta> dbMeta) {
        if (!dbMeta.isPresent()) {
            throw new IllegalArgumentException("Database must exist");
        }
        UserDatabaseMeta databaseMeta = dbMeta.get();
        if (!PermissionOracle.canCreateForm(databaseMeta.getDatabaseId(), databaseMeta)) {
            LOGGER.severe(() -> String.format("User %d does not have "
                            + Operation.CREATE_RESOURCE.name()
                            + " rights on Database %d",
                    databaseMeta.getUserId(),
                    databaseMeta.getLegacyDatabaseId()));
            throw new IllegalAccessCommandException();
        }
    }

    private void assertEditFormRights(ResourceId formId, Optional<UserDatabaseMeta> dbMeta) {
        if (!dbMeta.isPresent()) {
            throw new IllegalArgumentException("Database must exist");
        }
        UserDatabaseMeta databaseMeta = dbMeta.get();
        if (!PermissionOracle.canEditForm(formId, databaseMeta)) {
            LOGGER.severe(() -> String.format("User %d does not have "
                            + Operation.EDIT_RESOURCE.name()
                            + " rights on Database %d",
                    databaseMeta.getUserId(),
                    databaseMeta.getLegacyDatabaseId()));
            throw new IllegalAccessCommandException();
        }
    }

    private Optional<UserDatabaseMeta> getDatabaseMeta(PropertyMap properties, User user) {
        ResourceId databaseId = CuidAdapter.databaseId(properties.getRequiredInt("databaseId"));
        return databaseProvider.getDatabaseMetadata(databaseId, user.getId());
    }

    public Activity persist(Activity activity) {
        activityDAO.persist(activity);
        return activity;
    }

    @Override
    public void update(User user, Object entityId, PropertyMap changes) {
        Activity activity = em.find(Activity.class, entityId);
        Optional<UserDatabaseMeta> databaseMeta = databaseProvider.getDatabaseMetadata(
                CuidAdapter.databaseId(activity.getDatabase().getId()),
                user.getId());

        assertEditFormRights(activity.getFormId(), databaseMeta);

        activity.incrementSchemaVersion();

        UsageTracker.track(user.getId(), "update_activity", activity.getDatabase().getResourceId(), activity.getResourceId());


        applyProperties(activity, changes);
    }

    private Database getDatabase(PropertyMap properties) {
        return databaseDAO.findById(properties.getRequiredInt("databaseId"));
    }

    private LocationType getLocationType(PropertyMap properties) {
        return em.getReference(LocationType.class, properties.getRequiredInt(LOCATION_TYPE_ID_PROPERTY));
    }

    private Integer calculateNextSortOrderIndex(int databaseId) {
        Integer maxSortOrder = activityDAO.queryMaxSortOrder(databaseId);
        return maxSortOrder == null ? 1 : maxSortOrder + 1;
    }

    private void applyProperties(Activity activity, PropertyMap changes) {
        if (changes.containsKey(NAME_PROPERTY)) {
            String name = truncate(changes.get(NAME_PROPERTY));

            activity.setName(name);

            String json = JsonHelper.readJson(activity);

            updateFormClass(activity, name, json);
        }

        if (changes.containsKey(LOCATION_TYPE_ID_PROPERTY)) {
            LocationType location = em.find(LocationType.class, changes.get(LOCATION_TYPE_ID_PROPERTY));
            if (location != null) {
                activity.setLocationType(location);
            }
        }

        if (changes.containsKey(FOLDER_ID_PROPERTY)) {
            updateFolder(activity, changes);
        }

        if (changes.containsKey(CATEGORY_PROPERTY)) {
            updateCategory(activity, changes);
        }

        if (changes.containsKey(LOCATION_TYPE)) {
            LocationTypeDTO newLocationType = changes.get(LOCATION_TYPE);
            activity.setLocationType(em.getReference(LocationType.class, newLocationType.getId()));
        }

        if (changes.containsKey("mapIcon")) {
            activity.setMapIcon(changes.get("mapIcon"));
        }

        if (changes.containsKey(REPORTING_FREQUENCY_PROPERTY)) {
            activity.setReportingFrequency(changes.get(REPORTING_FREQUENCY_PROPERTY));
        }

        if (changes.containsKey(PUBLISHED_PROPERTY)) {
            activity.setPublished(changes.get(PUBLISHED_PROPERTY));
        }

        if (changes.containsKey(CLASSIC_VIEW_PROPERTY)) {
            activity.setClassicView(changes.get(CLASSIC_VIEW_PROPERTY));
        }

        if (changes.containsKey(SORT_ORDER_PROPERTY)) {
            activity.setSortOrder(changes.get(SORT_ORDER_PROPERTY));
        }

        if (isMetaChange(changes)) {
            activity.getDatabase().setLastMetaAndSchemaUpdate(new Date());
        } else {
            activity.getDatabase().setLastSchemaUpdate(new Date());
        }
    }

    /**
     * @return true if any change requested alters the {@link org.activityinfo.model.database.DatabaseMeta}
     */
    private boolean isMetaChange(PropertyMap changes) {
        if (changes.containsKey(NAME_PROPERTY)) {
            return true;
        }
        if (changes.containsKey(FOLDER_ID_PROPERTY) || changes.containsKey(CATEGORY_PROPERTY)) {
            return true;
        }
        if (changes.containsKey(PUBLISHED_PROPERTY)) {
            return true;
        }
        return false;
    }

    private void updateFolder(Activity activity, PropertyMap changes) {
        if(changes.get(FOLDER_ID_PROPERTY) == null) {
            activity.setFolder(null);
            activity.setCategory(null);
        } else {
            Folder folder = em.find(Folder.class, changes.get(FOLDER_ID_PROPERTY));
            if (folder != null && folder.getDatabase().getId() == activity.getDatabase().getId()) {
                activity.setFolder(folder);
                activity.setCategory(folder.getName());
            }
        }
    }

    private void updateCategory(Activity activity, PropertyMap changes) {
        String category = changes.get(CATEGORY_PROPERTY);
        if(category == null) {
            activity.setFolder(null);
            return;
        }

        // If the activity is already assigned to a folder of the same name,
        // there is nothing to do
        if(activity.getFolder() != null && activity.getFolder().getName().equalsIgnoreCase(category)) {
            return;
        }

        // Otherwise assign to an existing folder, or create a new one

        List<Folder> existingFolders = em.createQuery("SELECT f FROM Folder f WHERE f.database.id = :dbId AND f.name = :category", Folder.class)
                .setParameter("dbId", activity.getDatabase().getId())
                .setParameter(CATEGORY_PROPERTY, category)
                .getResultList();

        if(!existingFolders.isEmpty()) {
            activity.setFolder(existingFolders.get(0));
            return;
        }

        // Otherwise create a new folder...
        Folder folder = new Folder();
        folder.setDatabase(activity.getDatabase());
        folder.setName(category);
        em.persist(folder);

        activity.setFolder(folder);
    }

    private void updateFormClass(Activity activity, String name, String json) {
        if (!Strings.isNullOrEmpty(json)) {
            JsonParser parser = new JsonParser();
            JsonValue jsonObject = parser.parse(json);
            FormClass formClass = FormClass.fromJson(jsonObject);
            formClass.setLabel(name);

            String updatedJson = formClass.toJsonString();

            JsonHelper.updateWithJson(activity, updatedJson);
        }
    }
}
