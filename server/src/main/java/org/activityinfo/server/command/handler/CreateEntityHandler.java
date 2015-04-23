package org.activityinfo.server.command.handler;

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

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.activityinfo.legacy.shared.command.CreateEntity;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.server.command.handler.crud.ActivityPolicy;
import org.activityinfo.server.command.handler.crud.LocationTypePolicy;
import org.activityinfo.server.command.handler.crud.PropertyMap;
import org.activityinfo.server.command.handler.crud.UserDatabasePolicy;
import org.activityinfo.server.database.hibernate.entity.*;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.Map;

public class CreateEntityHandler extends BaseEntityHandler implements CommandHandler<CreateEntity> {

    private final Injector injector;
    private final KeyGenerator generator = new KeyGenerator();

    @Inject
    public CreateEntityHandler(EntityManager em, Injector injector) {
        super(em);
        this.injector = injector;
    }

    @Override
    public CommandResult execute(CreateEntity cmd, User user) throws CommandException {

        Map<String, Object> properties = cmd.getProperties().getTransientMap();
        PropertyMap propertyMap = new PropertyMap(cmd.getProperties().getTransientMap());

        if ("UserDatabase".equals(cmd.getEntityName())) {
            UserDatabasePolicy policy = injector.getInstance(UserDatabasePolicy.class);
            return new CreateResult((Integer) policy.create(user, propertyMap));
        } else if ("Activity".equals(cmd.getEntityName())) {
            ActivityPolicy policy = injector.getInstance(ActivityPolicy.class);
            return new CreateResult((Integer) policy.create(user, propertyMap));
        } else if ("AttributeGroup".equals(cmd.getEntityName())) {
            return createAttributeGroup(cmd, properties);
        } else if ("Attribute".equals(cmd.getEntityName())) {
            return createAttribute(cmd, properties);
        } else if ("Indicator".equals(cmd.getEntityName())) {
            return createIndicator(user, cmd, properties);
        } else if ("LocationType".equals(cmd.getEntityName())) {
            LocationTypePolicy policy = injector.getInstance(LocationTypePolicy.class);
            return new CreateResult(policy.create(user, propertyMap));
        } else {
            throw new CommandException("Invalid entity class " + cmd.getEntityName());
        }
    }

    private CommandResult createAttributeGroup(CreateEntity cmd, Map<String, Object> properties) {
        Activity activity = entityManager().find(Activity.class, properties.get("activityId"));

        AttributeGroup group = new AttributeGroup();
        group.setId(generator.generateInt());
        group.setSortOrder(activity.getAttributeGroups().size() + 1);
        updateAttributeGroupProperties(group, properties);

        entityManager().persist(group);

        activity.getAttributeGroups().add(group);

        activity.getDatabase().setLastSchemaUpdate(new Date());


        return new CreateResult(group.getId());
    }

    private CommandResult createAttribute(CreateEntity cmd, Map<String, Object> properties) {
        Attribute attribute = new Attribute();
        attribute.setId(generator.generateInt());
        AttributeGroup ag = entityManager().getReference(AttributeGroup.class, properties.get("attributeGroupId"));
        attribute.setGroup(ag);

        updateAttributeProperties(properties, attribute);

        Activity activity = ag.getActivities().iterator().next(); // Assume
        // group has
        // only one
        // activity

        entityManager().persist(attribute);
        activity.getDatabase().setLastSchemaUpdate(new Date());

        attribute.setSortOrder(ag.getAttributes().size());

        return new CreateResult(attribute.getId());
    }

    private CommandResult createIndicator(User user,
                                          CreateEntity cmd,
                                          Map<String, Object> properties) throws IllegalAccessCommandException {

        Indicator indicator = new Indicator();
        indicator.setId(generator.generateInt());
        Activity activity = entityManager().getReference(Activity.class, properties.get("activityId"));
        indicator.setActivity(activity);

        assertDesignPrivileges(user, indicator.getActivity().getDatabase());

        updateIndicatorProperties(indicator, properties);

        entityManager().persist(indicator);
        activity.getDatabase().setLastSchemaUpdate(new Date());

        return new CreateResult(indicator.getId());
    }
}
