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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.LocationDTO;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.Activity;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.LocationType;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.logging.Logger;

import static org.activityinfo.legacy.shared.model.EntityDTO.DATABASE_ID_PROPERTY;
import static org.activityinfo.legacy.shared.model.EntityDTO.NAME_PROPERTY;
import static org.activityinfo.legacy.shared.model.LocationTypeDTO.WORKFLOW_ID_PROPERTY;

public class LocationTypePolicy implements EntityPolicy<Activity> {

    private static final Logger LOGGER = Logger.getLogger(LocationTypePolicy.class.getName());

    private final EntityManager em;
    private final DatabaseProvider databaseProvider;

    @Inject
    public LocationTypePolicy(EntityManager em, DatabaseProvider databaseProvider) {
        this.em = em;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public Integer create(User user, PropertyMap properties) {
        int databaseId = properties.get(DATABASE_ID_PROPERTY);
        Database database = em.find(Database.class, databaseId);
        UserDatabaseMeta databaseMeta = databaseProvider.getDatabaseMetadata(
                CuidAdapter.databaseId(databaseId),
                user.getId());

        assertCreateFormRights(databaseMeta);

        // create the entity
        LocationType locationType = new LocationType();
        locationType.setVersion(1L);
        locationType.setName(properties.get(NAME_PROPERTY));
        locationType.setCountry(database.getCountry());
        locationType.setWorkflowId(properties.getOptionalString(WORKFLOW_ID_PROPERTY, LocationTypeDTO.CLOSED_WORKFLOW_ID));
        locationType.setDatabase(database);

        em.persist(locationType);
        
        
        return locationType.getId();
    }

    @Override
    public void update(User user, Object entityId, PropertyMap changes) {
        LocationType locationType = em.find(LocationType.class, entityId);
        int databaseId = locationType.getDatabase().getId();
        UserDatabaseMeta databaseMeta = databaseProvider.getDatabaseMetadata(
                CuidAdapter.databaseId(databaseId),
                user.getId());

        assertEditFormRights(locationType, databaseMeta);

        applyProperties(locationType, changes);
        
        locationType.incrementVersion();
    }

    private void assertCreateFormRights(UserDatabaseMeta databaseMeta) {
        if (!PermissionOracle.canCreateForm(databaseMeta.getDatabaseId(), databaseMeta)) {
            LOGGER.severe(String.format("User %d does not have "
                            + Operation.CREATE_FORM.name()
                            + " rights on Database %d",
                    databaseMeta.getUserId(),
                    databaseMeta.getLegacyDatabaseId()));
            throw new IllegalAccessCommandException();

        }
    }

    private void assertEditFormRights(LocationType locationType, UserDatabaseMeta databaseMeta) {
        ResourceId locationTypeForm = CuidAdapter.locationFormClass(locationType.getId());
        if (!PermissionOracle.canEditForm(locationTypeForm, databaseMeta)) {
            LOGGER.severe(String.format("User %d does not have "
                            + Operation.EDIT_FORM.name()
                            + " rights on Database %d",
                    databaseMeta.getUserId(),
                    databaseMeta.getLegacyDatabaseId()));
            throw new IllegalAccessCommandException();

        }
    }

    private void applyProperties(LocationType locationType, PropertyMap changes) {
        if (changes.containsKey(NAME_PROPERTY)) {
            locationType.setName(changes.get(NAME_PROPERTY));
        }
        if (changes.containsKey(WORKFLOW_ID_PROPERTY)) {
            String workflowId = changes.get(WORKFLOW_ID_PROPERTY);
            Preconditions.checkArgument(LocationDTO.isValidWorkflowId(workflowId), "invalid workflow id %s", workflowId);
            locationType.setWorkflowId(workflowId);
        }
        locationType.getDatabase().setLastSchemaUpdate(new Date());
    }
}
