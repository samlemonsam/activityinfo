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
package org.activityinfo.server.entity.change;

import com.google.common.collect.Maps;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.server.database.hibernate.entity.User;

import java.util.Map;
import java.util.Set;


/**
 * Wraps a legacy Command as a ChangeRequest
 */
public class ChangeRequestBuilder implements ChangeRequest {

    private AuthenticatedUser user;
    private ChangeType changeType;
    private String entityType;
    private String entityId;
    private final Map<String, Object> properties = Maps.newHashMap();

    public ChangeRequestBuilder() {
    }

    public static ChangeRequestBuilder delete() {
        return new ChangeRequestBuilder().setChangeType(ChangeType.DELETE);
    }

    public AuthenticatedUser getUser() {
        return user;
    }

    public ChangeRequestBuilder setUser(AuthenticatedUser user) {
        this.user = user;
        return this;
    }

    public ChangeRequestBuilder setChangeType(ChangeType changeType) {
        this.changeType = changeType;
        return this;
    }

    public ChangeRequestBuilder setEntityType(String entityType) {
        this.entityType = entityType;
        return this;
    }

    public ChangeRequestBuilder setEntityId(int entityId) {
        return setEntityId(Integer.toString(entityId));
    }

    public ChangeRequestBuilder setEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    @Override
    public AuthenticatedUser getRequestingUser() {
        return user;
    }

    @Override
    public ChangeType getChangeType() {
        return changeType;
    }

    @Override
    public String getEntityType() {
        return entityType;
    }

    @Override
    public <T> T getPropertyValue(Class<T> propertyClass, String propertyName) {
        if (!properties.containsKey(propertyName)) {
            throw new ChangeException(ChangeFailureType.REQUIRED_PROPERTY_MISSING, propertyName);
        }
        Object value = properties.get(propertyName);
        if (value != null && !value.getClass().equals(propertyClass)) {
            throw new ChangeException(ChangeFailureType.MALFORMED_PROPERTY, propertyName);
        }
        return (T) value;
    }

    @Override
    public Set<String> getUpdatedProperties() {
        return properties.keySet();
    }

    public ChangeRequestBuilder setProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    @Override
    public String getEntityId() {
        return entityId;
    }

    public ChangeRequestBuilder setUser(User user) {
        return setUser(new AuthenticatedUser("xyz", user.getId(), user.getEmail()));
    }

    public ChangeRequestBuilder setProperties(Map<String, Object> map) {
        properties.putAll(map);
        return this;
    }
}
