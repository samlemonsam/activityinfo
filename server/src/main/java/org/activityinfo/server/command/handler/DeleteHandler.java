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
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.server.database.hibernate.entity.*;

import javax.persistence.EntityManager;

public class DeleteHandler implements CommandHandler<Delete> {

    private EntityManager em;
    private PermissionOracle permissionOracle;

    @Inject
    public DeleteHandler(EntityManager em, PermissionOracle permissionOracle) {
        this.em = em;
        this.permissionOracle = permissionOracle;
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
        permissionOracle.assertDeletionAuthorized(entity, user);

        // Mark the entity as deleted
        entity.delete();

        if (entity instanceof HardDeleteable) {
            em.remove(entity);
        }

        return null;
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
