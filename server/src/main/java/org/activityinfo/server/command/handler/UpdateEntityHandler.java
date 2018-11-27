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
package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.activityinfo.legacy.shared.command.UpdateEntity;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.server.command.handler.crud.ActivityPolicy;
import org.activityinfo.server.command.handler.crud.LocationTypePolicy;
import org.activityinfo.server.command.handler.crud.PropertyMap;
import org.activityinfo.server.command.handler.crud.UserDatabasePolicy;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Alex Bertram
 * @see org.activityinfo.legacy.shared.command.UpdateEntity
 */
public class UpdateEntityHandler extends BaseEntityHandler implements CommandHandler<UpdateEntity> {

    private static final Logger LOGGER = Logger.getLogger(UpdateEntityHandler.class.getName());

    private final Injector injector;

    @Inject
    public UpdateEntityHandler(EntityManager em,
                               DatabaseProvider databaseProvider,
                               Injector injector) {
        super(em, databaseProvider);
        this.injector = injector;
    }

    @Override
    public CommandResult execute(UpdateEntity cmd, User user) {

        LOGGER.fine("[execute] Update command for entity: " + cmd.getEntityName() + ".");

        Map<String, Object> changes = cmd.getChanges().getTransientMap();
        PropertyMap changeMap = new PropertyMap(changes);

        switch (cmd.getEntityName()) {

            case UserDatabaseDTO.ENTITY_NAME:
                updateDatabase(cmd, user, changeMap);
                break;

            case ActivityDTO.ENTITY_NAME:
                updateActivity(cmd, user, changeMap);
                break;

            case FolderDTO.ENTITY_NAME:
                updateFolder(user, cmd.getId(), changeMap);
                break;

            case AttributeGroupDTO.ENTITY_NAME:
                updateAttributeGroup(user, cmd, changes);
                break;

            case AttributeDTO.ENTITY_NAME:
                updateAttribute(user, cmd, changes);
                break;

            case IndicatorDTO.ENTITY_NAME:
                updateIndicator(user, cmd, changes);
                break;

            case LockedPeriodDTO.ENTITY_NAME:
                updateLockedPeriod(user, cmd, changes);
                break;

            case TargetDTO.ENTITY_NAME:
                updateTarget(user, cmd, changes);
                break;

            case LocationTypeDTO.ENTITY_NAME:
                updateLocationType(cmd, user, changeMap);
                break;

            default:
                throw new UnsupportedOperationException("EntityType:" + cmd.getEntityName());
        }

        return null;
    }

    private void updateLocationType(UpdateEntity cmd, User user, PropertyMap changeMap) {
        LocationTypePolicy policy = injector.getInstance(LocationTypePolicy.class);
        policy.update(user, cmd.getId(), changeMap);
    }

    private void updateActivity(UpdateEntity cmd, User user, PropertyMap changeMap) {
        ActivityPolicy policy = injector.getInstance(ActivityPolicy.class);
        policy.update(user, cmd.getId(), changeMap);
    }

    private void updateDatabase(UpdateEntity cmd, User user, PropertyMap changeMap) {
        UserDatabasePolicy policy = injector.getInstance(UserDatabasePolicy.class);
        policy.update(user, cmd.getId(), changeMap);
    }

    private void updateFolder(User user, int id, PropertyMap changeMap) {

        Folder folder = entityManager().find(Folder.class, id);

        assertEditFolderRights(user, folder);

        if(changeMap.containsKey("name")) {
            String newName = changeMap.get("name");

            folder.setName(newName);

            entityManager().createNativeQuery("UPDATE activity a SET category = :name WHERE a.folderId = :folderId")
                    .setParameter("name", newName)
                    .setParameter("folderId", folder.getId())
                    .executeUpdate();

        }

        if(changeMap.containsKey("sortOrder")) {
            folder.setSortOrder(changeMap.get("sortOrder"));
        }
    }

    private void updateIndicator(User user,
                                 UpdateEntity cmd,
                                 Map<String, Object> changes) {
        Indicator indicator = entityManager().find(Indicator.class, cmd.getId());

        assertEditFormRights(user, indicator.getActivity());

        updateIndicatorProperties(indicator, changes);
    }

    private void updateLockedPeriod(User user, UpdateEntity cmd, Map<String, Object> changes) {
        LockedPeriod lockedPeriod = entityManager().find(LockedPeriod.class, cmd.getId());

        assertLockRecordsRights(user, lockedPeriod);

        updateLockedPeriodProperties(lockedPeriod, changes);
    }

    private void updateAttribute(User user, UpdateEntity cmd, Map<String, Object> changes) {
        Attribute attribute = entityManager().find(Attribute.class, cmd.getId());

        assertEditFormRights(user, attribute.getGroup());

        updateAttributeProperties(changes, attribute);
        AttributeGroup ag = entityManager().find(AttributeGroup.class, attribute.getGroup().getId());
        Activity activity = ag.getActivities().iterator().next(); // Assume only one activity for the attr group
        activity.incrementSchemaVersion();
        activity.getDatabase().setLastSchemaUpdate(new Date());
    }

    private void updateAttributeGroup(User user, UpdateEntity cmd, Map<String, Object> changes) {
        AttributeGroup group = entityManager().find(AttributeGroup.class, cmd.getId());

        assertEditFormRights(user, group);

        updateAttributeGroupProperties(group, changes);

        Activity activity = group.getActivities().iterator().next(); // Assume only one activity for the attr group
        activity.incrementSchemaVersion();
        activity.getDatabase().setLastSchemaUpdate(new Date());
    }

    private void updateTarget(User user, UpdateEntity cmd, Map<String, Object> changes) {
        Target target = entityManager().find(Target.class, cmd.getId());

        assertManageTargetsRights(user, target.getDatabase());

        updateTargetProperties(target, changes);

        target.getDatabase().setLastSchemaUpdate(new Date());
    }
}
