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
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.legacy.shared.model.LocationDTO;

import java.util.Map;

public class CreateLocation implements MutatingCommand<VoidResult> {

    private RpcMap properties;

    public CreateLocation() {
        properties = new RpcMap();
    }

    public CreateLocation(LocationDTO location) {
        properties = new RpcMap();
        properties.put("id", location.getId());
        properties.put("locationTypeId", location.getLocationTypeId());
        properties.put("name", location.getName());
        properties.put("axe", location.getAxe());
        properties.put("latitude", location.getLatitude());
        properties.put("longitude", location.getLongitude());

        for (AdminEntityDTO entity : location.getAdminEntities()) {
            properties.put(AdminLevelDTO.getPropertyName(entity.getLevelId()), entity.getId());
        }
    }

    public CreateLocation(Map<String, Object> properties) {
        this.properties = new RpcMap();
        this.properties.putAll(properties);
    }

    public RpcMap getProperties() {
        return properties;
    }

    public void setProperties(RpcMap properties) {
        this.properties = properties;
    }

    public int getLocationId() {
        return (Integer) properties.get("id");
    }

    public int getLocationTypeId() {
        return (Integer) properties.get("locationTypeId");
    }
}
