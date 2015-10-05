package org.activityinfo.model.lock;
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

import org.activityinfo.model.resource.IsResource;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;

/**
 * @author yuriyz on 10/05/2015.
 */
public class ResourceLock implements IsResource {

    private ResourceId id;
    private ResourceId ownerId;

    private String name;
    private String expression;

    public ResourceLock() {
    }

    @Override
    public ResourceId getId() {
        return id;
    }

    public void setId(ResourceId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public Resource asResource() {
        Resource resource = Resources.createResource();
        resource.setId(id);
        resource.setOwnerId(ownerId);
        resource.set("name", name);
        resource.set("expression", expression);

        return resource;
    }

    public static ResourceLock asLock(Resource resource) {
        ResourceLock lock = new ResourceLock();
        lock.id = resource.getId();
        lock.ownerId = resource.getOwnerId();
        lock.name = resource.getString("name");
        lock.expression = resource.getString("expression");
        return lock;
    }

    @Override
    public String toString() {
        return "ResourceLock{" +
                "name='" + name + '\'' +
                ", expression='" + expression + '\'' +
                '}';
    }
}
