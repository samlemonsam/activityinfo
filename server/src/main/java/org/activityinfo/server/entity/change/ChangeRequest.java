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

import org.activityinfo.legacy.shared.AuthenticatedUser;

import java.util.Set;

/**
 * Encapsulates a change request to a single entity
 * on behalf of an authenticated user.
 */
public interface ChangeRequest {

    AuthenticatedUser getRequestingUser();

    ChangeType getChangeType();

    String getEntityType();

    /**
     * @return the id of the entity to create, modify, or delete
     */
    String getEntityId();

    /**
     * @return set of names of new or updated properties
     */
    Set<String> getUpdatedProperties();

    /**
     * Gets the new/updated value for the entity's property named
     * {@code propertyName), converting if possible to {@code propertyClass}
     *
     * @param propertyClass the Java type in which to return the value.
     * @param propertyName  the name of the property to retrieve
     * @return the new value of the property
     * @throws ChangeException if the ChangeRequest does not include
     *                         this property.
     */
    <T> T getPropertyValue(Class<T> propertyClass, String propertyName);
}
