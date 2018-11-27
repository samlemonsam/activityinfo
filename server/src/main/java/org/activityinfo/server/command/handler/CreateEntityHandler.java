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
import org.activityinfo.legacy.shared.command.CreateEntity;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.server.command.handler.crud.ActivityPolicy;
import org.activityinfo.server.command.handler.crud.LocationTypePolicy;
import org.activityinfo.server.command.handler.crud.PropertyMap;
import org.activityinfo.server.command.handler.crud.UserDatabasePolicy;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.persistence.EntityManager;
import javax.persistence.QueryTimeoutException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateEntityHandler extends BaseEntityHandler implements CommandHandler<CreateEntity> {

    private static final Logger LOGGER = Logger.getLogger(CreateEntityHandler.class.getName());

    private final Injector injector;
    private final KeyGenerator generator = new KeyGenerator();

    @Inject
    public CreateEntityHandler(EntityManager em,
                               DatabaseProvider databaseProvider,
                               Injector injector) {
        super(em, databaseProvider);
        this.injector = injector;
    }

    @Override
    public CommandResult execute(CreateEntity cmd, User user) {

        Map<String, Object> properties = cmd.getProperties().getTransientMap();
        PropertyMap propertyMap = new PropertyMap(cmd.getProperties().getTransientMap());

        switch (cmd.getEntityName()) {
            case UserDatabaseDTO.ENTITY_NAME:
                return createDatabase(user, propertyMap);

            case FolderDTO.ENTITY_NAME:
                return createFolder(user, properties);

            case ActivityDTO.ENTITY_NAME:
                return createActivity(user, propertyMap);

            case AttributeGroupDTO.ENTITY_NAME:
                return createAttributeGroup(properties);

            case AttributeDTO.ENTITY_NAME:
                return createAttribute(properties);

            case IndicatorDTO.ENTITY_NAME:
                return createIndicator(user, properties);

            case LocationTypeDTO.ENTITY_NAME:
                return createLocationType(user, propertyMap);

            default:
                throw new CommandException("Invalid entity class " + cmd.getEntityName());
        }
    }

    private CommandResult createDatabase(User user, PropertyMap propertyMap) {
        UserDatabasePolicy policy = injector.getInstance(UserDatabasePolicy.class);
        return new CreateResult((Integer) policy.create(user, propertyMap));
    }

    private CommandResult createActivity(User user, PropertyMap propertyMap) {
        ActivityPolicy policy = injector.getInstance(ActivityPolicy.class);
        return new CreateResult((Integer) policy.create(user, propertyMap));
    }

    private CommandResult createLocationType(User user, PropertyMap propertyMap) {
        LocationTypePolicy policy = injector.getInstance(LocationTypePolicy.class);
        return new CreateResult(policy.create(user, propertyMap));
    }


    private CommandResult createFolder(User user, Map<String, Object> properties) {
        Integer databaseId = (Integer) properties.get("databaseId");
        String name = (String) properties.get(EntityDTO.NAME_PROPERTY);

        Database database = entityManager().find(Database.class, databaseId);

        assertCreateFolderRights(user, database);

        Folder folder = new Folder();
        folder.setDatabase(database);
        folder.setName(name);

        entityManager().persist(folder);

        return new CreateResult(folder.getId());
    }

    private CommandResult createAttributeGroup(Map<String, Object> properties) {
        Activity activity = entityManager().find(Activity.class, properties.get("activityId"));

        AttributeGroup group = new AttributeGroup();
        group.setId(generator.generateInt());
        group.setSortOrder(activity.getAttributeGroups().size() + 1);
        updateAttributeGroupProperties(group, properties);

        entityManager().persist(group);

        activity.getAttributeGroups().add(group);

        activity.incrementSchemaVersion();
        activity.getDatabase().setLastSchemaUpdate(new Date());


        return new CreateResult(group.getId());
    }

    private CommandResult createAttribute(Map<String, Object> properties) {
        Attribute attribute = new Attribute();
        attribute.setId(generator.generateInt());
        AttributeGroup ag = entityManager().getReference(AttributeGroup.class, properties.get("attributeGroupId"));
        attribute.setGroup(ag);

        updateAttributeProperties(properties, attribute);
        
        if(attribute.getSortOrder() == 0) {
            attribute.setSortOrder(queryNextAttributeOrdinal(ag));
        }

        Activity activity = ag.getActivities().iterator().next(); // Assume group has only one activity

        entityManager().persist(attribute);

        activity.incrementSchemaVersion();
        activity.getDatabase().setLastSchemaUpdate(new Date());

        return new CreateResult(attribute.getId());
    }


    private CommandResult createIndicator(User user, Map<String, Object> properties)  {

        // query the next indicator sort order index
        
        Indicator indicator = new Indicator();
        indicator.setId(generator.generateInt());
        Activity activity = entityManager().getReference(Activity.class, properties.get("activityId"));
        indicator.setActivity(activity);

        assertEditFormRights(user, activity);
        
        updateIndicatorProperties(indicator, properties);
        
        if(indicator.getSortOrder() == 0) {
            indicator.setSortOrder(queryNextIndicatorSortOrdinal(activity));
        }

        entityManager().persist(indicator);
        
        activity.incrementSchemaVersion();
        activity.getDatabase().setLastSchemaUpdate(new Date());

        return new CreateResult(indicator.getId());
    }

    private int queryNextIndicatorSortOrdinal(Activity activity) {
        try {

            Integer nextOrdinal = entityManager()
                    .createQuery("select max(i.sortOrder) from Indicator i where i.activity = :activity", Integer.class)
                    .setParameter("activity", activity)
                    .getSingleResult();

            if (nextOrdinal == null) {
                return 1;
            } else {
                return nextOrdinal + 1;
            }
        } catch (QueryTimeoutException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return 1;
        }
    }


    private int queryNextAttributeOrdinal(AttributeGroup ag) {
        Integer nextOrdinal = entityManager()
                .createQuery("select max(a.sortOrder) from Attribute a where a.group = :group", Integer.class)
                .setParameter("group", ag)
                .getSingleResult();
        
        if(nextOrdinal == null) {
            return 1; 
        } else {
            return nextOrdinal+1;
        }
    
    }
}
