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
package org.activityinfo.server.database.hibernate;

import com.google.common.collect.Maps;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;

import javax.persistence.EntityManager;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkState;

/**
 * Custom scope for the Hibernate session.
 * <p/>
 * <p/>
 * This scope is used to ensure that each request gets its own EntityManager
 * (see {@link HibernateSessionFilter}. However, it is provided separately from @RequestScope
 * because EntityManger's are also needed during batch processing.
 */
public class HibernateSessionScope implements Scope {

    private static final Logger LOGGER = Logger.getLogger(HibernateSessionScope.class.getName());

    private final ThreadLocal<Map<Key<?>, Object>> values = new ThreadLocal<Map<Key<?>, Object>>();

    public void enter() {
        checkState(values.get() == null, "A hibernate session block is already in progress");
        values.set(Maps.<Key<?>, Object>newHashMap());
    }

    public void exit() {
        checkState(values.get() != null, "No hibernate session block in progress");

        // close session
        try {
            if (values.get().containsKey(Key.get(EntityManager.class))) {
                EntityManager em = (EntityManager) values.get().get(Key.get(EntityManager.class));
                em.close();
            }

        } catch (Exception caught) {
            LOGGER.log(Level.SEVERE, "Error closing connection", caught);

        } finally {
            values.remove();
        }

    }

    @Override
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return new Provider<T>() {
            @Override
            public T get() {
                Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);

                @SuppressWarnings("unchecked") T current = (T) scopedObjects.get(key);
                if (current == null && !scopedObjects.containsKey(key)) {
                    current = unscoped.get();
                    scopedObjects.put(key, current);
                }
                return current;
            }
        };
    }

    private <T> Map<Key<?>, Object> getScopedObjectMap(Key<T> key) {
        Map<Key<?>, Object> scopedObjects = values.get();
        if (scopedObjects == null) {
            throw new OutOfScopeException("Cannot access " + key + " outside of a hibernate session block");
        }
        return scopedObjects;
    }
}
