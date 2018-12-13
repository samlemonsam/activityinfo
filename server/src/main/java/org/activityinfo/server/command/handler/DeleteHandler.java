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
import org.activityinfo.legacy.shared.command.Delete;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteHandler implements CommandHandler<Delete> {

    private static final Logger LOGGER = Logger.getLogger(DeleteHandler.class.getName());

    private EntityManager em;
    private DatabaseProvider provider;

    @Inject
    public DeleteHandler(EntityManager em, DatabaseProvider databaseProvider) {
        this.em = em;
        this.provider = databaseProvider;
    }

    @Override
    public CommandResult execute(Delete cmd, User user) {
        Class<? extends Deleteable> entityClass = entityClassForEntityName(cmd.getEntityName());
        Deleteable entity = em.find(entityClass, cmd.getId());
        
        if(entity == null) {
            throw new CommandException(String.format("%s with id %d does not exist.", 
                    cmd.getEntityName(), cmd.getId()));
        }

        // Ensure that the user is authorized to perform deletion
        assertDeletionAuthorized(entity, user);

        // Mark the entity as deleted
        entity.delete();

        if (entity instanceof HardDeleteable) {
            em.remove(entity);
        }

        return null;
    }

    private void assertDeletionAuthorized(Deleteable entity, User user) {
        if(entity instanceof Database) {
            assertDatabaseDeletionAuthorized(((Database) entity), user);

        } else if(entity instanceof Site) {
            assertDeleteSiteRights(((Site) entity), user.getId());

        } else if(entity instanceof Activity) {
            assertDeleteFormRights((Activity) entity, user.getId());

        } else if(entity instanceof Indicator) {
            assertEditFormRights(((Indicator) entity).getActivity(), user.getId());

        } else if(entity instanceof AttributeGroup) {
            assertEditFormRights(((AttributeGroup) entity), user.getId());

        } else if(entity instanceof Attribute) {
            assertEditFormRights(((Attribute) entity).getGroup(), user.getId());

        } else if(entity instanceof Project) {
            assertDeleteProjectRights(((Project) entity).getDatabase(), user.getId());

        } else if(entity instanceof LockedPeriod) {
            assertLockRecordsRights(((LockedPeriod) entity), user.getId());

        } else if(entity instanceof Target) {
            assertManageTargetsRights(((Target) entity), user.getId());

        } else if(entity instanceof TargetValue) {
            assertManageTargetsRights(((TargetValue) entity).getTarget(), user.getId());

        } else if(entity instanceof LocationType) {
            assertDeleteFormRights(((LocationType) entity), user.getId());

        } else if(entity instanceof Folder) {
            assertDeleteFolderRights(((Folder) entity), user.getId());

        } else {
            LOGGER.log(Level.SEVERE, String.format("Unable to determine permissions for deleting entity of type %s",
                    entity.getClass().getName()));
            throw new UnsupportedOperationException();
        }
    }

    private void assertDatabaseDeletionAuthorized(Database entity, User user) {
        ResourceId databaseId = CuidAdapter.databaseId(entity.getId());
        Optional<UserDatabaseMeta> databaseMeta = provider.getDatabaseMetadata(databaseId, user.getId());

        if (!databaseMeta.isPresent()) {
            throw new IllegalArgumentException("DatabaseMeta must exist");
        }
        if (!PermissionOracle.canDeleteDatabase(databaseMeta.get())) {
            LOGGER.severe(String.format("User %d is not authorized to delete " +
                    "database %d: it is owned by user %d",
                    user.getId(),
                    entity.getId(),
                    entity.getOwner().getId()));
            throw new IllegalAccessCommandException();
        }
    }

    public void assertDeleteSiteRights(Site site, int user) {
        ResourceId databaseId = CuidAdapter.databaseId(site.getActivity().getDatabase().getId());
        Optional<UserDatabaseMeta> databaseMeta = provider.getDatabaseMetadata(databaseId, user);
        ResourceId activityId = site.getActivity().getFormId();
        int partnerId = site.getPartner().getId();

        if (!databaseMeta.isPresent()) {
            throw new IllegalArgumentException("DatabaseMeta must exist");
        }
        if (!PermissionOracle.canDeleteSite(activityId, partnerId, databaseMeta.get())) {
            LOGGER.severe(String.format("User %d does not have "
                            + Operation.DELETE_RECORD.name()
                            + " rights on Activity %d",
                    databaseMeta.get().getUserId(),
                    site.getActivity().getId()));
            throw new IllegalAccessCommandException();
        }
    }

    public void assertEditFormRights(AttributeGroup group, int user) {
        if (group.getActivities().isEmpty()) {
            LOGGER.severe(String.format("Unable to check authorization to delete attribute group %s" +
                    ": there are no associated activities.",
                    group.getName()));
            throw new IllegalAccessCommandException();
        }
        for (Activity activity : group.getActivities()) {
            assertEditFormRights(activity, user);
        }
    }

    public void assertEditFormRights(Activity activity, int user) {
        ResourceId databaseId = CuidAdapter.databaseId(activity.getDatabase().getId());
        Optional<UserDatabaseMeta> databaseMeta = provider.getDatabaseMetadata(databaseId, user);
        ResourceId activityId = activity.getFormId();

        if (!databaseMeta.isPresent()) {
            throw new IllegalArgumentException("DatabaseMeta must exist");
        }
        if (!PermissionOracle.canEditForm(activityId, databaseMeta.get())) {
            LOGGER.severe(String.format("User %d does not have "
                            + Operation.EDIT_RESOURCE.name()
                            + " rights on Activity %d",
                    databaseMeta.get().getUserId(),
                    activity.getId()));
            throw new IllegalAccessCommandException();
        }
    }

    public void assertDeleteFormRights(Activity activity, int user) {
        ResourceId databaseId = CuidAdapter.databaseId(activity.getDatabase().getId());
        Optional<UserDatabaseMeta> databaseMeta = provider.getDatabaseMetadata(databaseId, user);

        if (!databaseMeta.isPresent()) {
            throw new IllegalArgumentException("DatabaseMeta must exist");
        }
        if (!PermissionOracle.canDeleteForm(activity.getFormId(), databaseMeta.get())) {
            LOGGER.severe(String.format("User %d does not have "
                            + Operation.DELETE_RESOURCE.name()
                            + " rights on Activity %d",
                    databaseMeta.get().getUserId(),
                    activity.getId()));
            throw new IllegalAccessCommandException();
        }
    }

    public void assertDeleteProjectRights(Database database, int user) {
        ResourceId databaseId = CuidAdapter.databaseId(database.getId());
        Optional<UserDatabaseMeta> databaseMeta = provider.getDatabaseMetadata(databaseId, user);

        if (!databaseMeta.isPresent()) {
            throw new IllegalArgumentException("DatabaseMeta must exist");
        }
        if (!PermissionOracle.canDeleteForm(databaseId, databaseMeta.get())) {
            LOGGER.severe(String.format("User %d does not have "
                            + Operation.DELETE_RESOURCE.name()
                            + " rights on Database %d",
                    databaseMeta.get().getUserId(),
                    database.getId()));
            throw new IllegalAccessCommandException();
        }
    }

    public void assertLockRecordsRights(LockedPeriod lockedPeriod, int user) {
        ResourceId databaseId = CuidAdapter.databaseId(lockedPeriod.getDatabase().getId());
        Optional<UserDatabaseMeta> databaseMeta = provider.getDatabaseMetadata(databaseId, user);

        if (!databaseMeta.isPresent()) {
            throw new IllegalArgumentException("DatabaseMeta must exist");
        }
        if (!PermissionOracle.canLockRecords(lockedPeriod.getResourceId(), databaseMeta.get())) {
            LOGGER.severe(String.format("User %d does not have "
                            + Operation.LOCK_RECORDS.name()
                            + " rights on Resource %s",
                    databaseMeta.get().getUserId(),
                    lockedPeriod.getResourceId().asString()));
            throw new IllegalAccessCommandException();
        }
    }

    public void assertManageTargetsRights(Target target, int user) {
        ResourceId databaseId = CuidAdapter.databaseId(target.getDatabase().getId());
        Optional<UserDatabaseMeta> databaseMeta = provider.getDatabaseMetadata(databaseId, user);

        if (!databaseMeta.isPresent()) {
            throw new IllegalArgumentException("DatabaseMeta must exist");
        }
        if (!PermissionOracle.canManageTargets(databaseId, databaseMeta.get())) {
            LOGGER.severe(() -> String.format("User %d does not have "
                            + Operation.MANAGE_TARGETS.name()
                            + " rights on Database %d",
                    databaseMeta.get().getUserId(),
                    databaseMeta.get().getLegacyDatabaseId()));
            throw new IllegalAccessCommandException();
        }
    }

    public void assertDeleteFormRights(LocationType locationType, int user) {
        ResourceId databaseId = CuidAdapter.databaseId(locationType.getDatabase().getId());
        ResourceId locationTypeForm = CuidAdapter.locationFormClass(locationType.getId());
        Optional<UserDatabaseMeta> databaseMeta = provider.getDatabaseMetadata(databaseId, user);

        if (!databaseMeta.isPresent()) {
            throw new IllegalArgumentException("DatabaseMeta must exist");
        }
        if (!PermissionOracle.canDeleteForm(locationTypeForm, databaseMeta.get())) {
            LOGGER.severe(() -> String.format("User %d does not have "
                            + Operation.DELETE_RESOURCE.name()
                            + " rights on Database %d",
                    databaseMeta.get().getUserId(),
                    databaseMeta.get().getLegacyDatabaseId()));
            throw new IllegalAccessCommandException();
        }
    }

    public void assertDeleteFolderRights(Folder folder, int user) {
        ResourceId databaseId = CuidAdapter.databaseId(folder.getDatabase().getId());
        ResourceId folderId = CuidAdapter.folderId(folder.getId());
        Optional<UserDatabaseMeta> databaseMeta = provider.getDatabaseMetadata(databaseId, user);

        if (!databaseMeta.isPresent()) {
            throw new IllegalArgumentException("DatabaseMeta must exist");
        }
        if (!PermissionOracle.canDeleteFolder(folderId, databaseMeta.get())) {
            LOGGER.severe(String.format("User %d does not have "
                            + Operation.DELETE_RESOURCE.name()
                            + " rights on Folder %d",
                    databaseMeta.get().getUserId(),
                    folder.getId()));
            throw new IllegalAccessCommandException();
        }
    }

    private Class<? extends Deleteable> entityClassForEntityName(String entityName) {
        switch (entityName) {
            case ActivityDTO.ENTITY_NAME:
                return Activity.class;

            case AttributeDTO.ENTITY_NAME:
                return Attribute.class;

            case AttributeGroupDTO.ENTITY_NAME:
                return AttributeGroup.class;

            case FolderDTO.ENTITY_NAME:
                return Folder.class;

            case IndicatorDTO.ENTITY_NAME:
                return Indicator.class;

            case LocationTypeDTO.ENTITY_NAME:
                return LocationType.class;

            case LockedPeriodDTO.ENTITY_NAME:
                return LockedPeriod.class;

            case ProjectDTO.ENTITY_NAME:
                return Project.class;

            case SiteDTO.ENTITY_NAME:
                return Site.class;

            case TargetDTO.ENTITY_NAME:
                return Target.class;

            case TargetValueDTO.ENTITY_NAME:
                return TargetValue.class;

            case UserDatabaseDTO.ENTITY_NAME:
                return Database.class;

            default:
                throw new IllegalArgumentException(entityName);
        }
    }
}
