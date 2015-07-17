package org.activityinfo.legacy.shared.command;

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

import com.extjs.gxt.ui.client.data.RpcMap;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.model.*;

import java.util.Map;

/**
 * Creates and persists a domain entity on the server.
 * <p/>
 * Note: Some entities require specialized commands to create or update, such
 * as:
 * <ul>
 * <li>{@link org.activityinfo.legacy.shared.command.AddPartner}</li>
 * <li>{@link UpdateUserPermissions}</li>
 * <li>{@link org.activityinfo.legacy.shared.command.CreateReport}</li>
 * </ul>
 * <p/>
 * Returns {@link org.activityinfo.legacy.shared.command.result.CreateResult}
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class CreateEntity implements MutatingCommand<CreateResult> {
    private String entityName;
    private RpcMap properties;

    private AdminEntityDTO entity_;
    private PartnerDTO partner_;
    private LocationTypeDTO locationType_;

    public CreateEntity() {

    }

    public CreateEntity(String entityName, Map<String, ?> properties) {
        this.entityName = entityName;
        this.properties = new RpcMap();
        this.properties.putAll(properties);
    }

    public CreateEntity(EntityDTO entity) {
        this.entityName = entity.getEntityName();
        this.properties = new RpcMap();
        this.properties.putAll(entity.getProperties());
    }

    /**
     * @return The name of the entity to create. The name should correspond to
     * one of the classes in {@link org.activityinfo.server.domain}
     */
    public String getEntityName() {
        return entityName;
    }

    protected void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * A map of properties to create.
     * <p/>
     * Note: For the most part, references to related entities should be
     * specified by id: for example,
     * {@link org.activityinfo.server.database.hibernate.entity.Activity#database}
     * should be entered as databaseId in the property map.
     * <p/>
     * There are some exceptions to this that will take some time to fix:
     * <ul>
     * <li>
     * {@link org.activityinfo.server.database.hibernate.entity.Site#partner}</li>
     * <li>AdminEntities associated with Sites/Locations</li>
     * </ul>
     * See {@link org.activityinfo.server.command.handler.CreateEntityHandler}
     * for the last word.
     *
     * @return The properties/fields of the entity to create.
     */
    public RpcMap getProperties() {
        return properties;
    }

    protected void setProperties(RpcMap properties) {
        this.properties = properties;
    }

    public static CreateEntity Activity(UserDatabaseDTO db, ActivityFormDTO act) {
        CreateEntity cmd = new CreateEntity("Activity", act.getProperties());
        cmd.properties.put("locationTypeId", act.getLocationTypeId());
        cmd.properties.put("databaseId", db.getId());
        return cmd;
    }
}
